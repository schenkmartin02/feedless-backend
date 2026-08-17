package gg.feedless.backend.crawl;

import gg.feedless.backend.match.Match;
import gg.feedless.backend.match.MatchRepository;
import gg.feedless.backend.player.Player;
import gg.feedless.backend.player.PlayerRankRepository;
import gg.feedless.backend.player.PlayerRepository;
import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.dto.account.AccountDto;
import gg.feedless.backend.riot.dto.league.LeagueEntryDto;
import gg.feedless.backend.riot.dto.match.MatchDto;
import gg.feedless.backend.riot.dto.summoner.SummonerDto;
import gg.feedless.backend.stats.RegionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class CrawlWorker {
    private static final Logger log = LoggerFactory.getLogger(CrawlWorker.class);

    private final CrawlJobRepository crawlJobRepository;
    private final PlayerRepository playerRepository;
    private final PlayerRankRepository playerRankRepository;
    private final MatchRepository matchRepository;

    private final RiotApiClient riotApiClient;

    private final MatchIngestService matchIngestService;

    private final ExecutorService matchExecutorService;
    private final ExecutorService rankExecutorService;

    private final int profileTTLInDays;
    private final int recrawlTTLInDays;
    private final int batchSize;
    private final int maxRetries;

    private static final int NEW_CRAWL_MATCH_LIST = 20;
    private static final int RE_CRAWL_MATCH_LIST = 100;
    private static final int MATCH_LIST_START_TIME = 30;

    public CrawlWorker(CrawlJobRepository crawlJobRepository, RiotApiClient riotApiClient, PlayerRepository playerRepository, PlayerRankRepository playerRankRepository, MatchRepository matchRepository, MatchIngestService matchIngestService, @Value("${crawler.profile-refresh-ttl-days}") int profileTTLInDays, @Value("${crawler.recrawl.ttl-days}") int recrawlTTLInDays, @Value("${crawler.recrawl.batch-size}") int batchSize, @Qualifier("matchFetchExecutor") ExecutorService matchExecutorService, @Qualifier("rankFetchExecutor") ExecutorService rankExecutorService, @Value("${crawler.max-retries}") int maxRetries) {
        this.crawlJobRepository = crawlJobRepository;
        this.riotApiClient = riotApiClient;
        this.playerRepository = playerRepository;
        this.playerRankRepository = playerRankRepository;
        this.matchRepository = matchRepository;
        this.matchIngestService = matchIngestService;
        this.profileTTLInDays = profileTTLInDays;
        this.recrawlTTLInDays = recrawlTTLInDays;
        this.batchSize = batchSize;
        this.matchExecutorService = matchExecutorService;
        this.rankExecutorService = rankExecutorService;
        this.maxRetries = maxRetries;
    }

    @Scheduled(fixedDelay = 100)
    public void crawlerEUNE(){
        tick(RegionType.EUNE);
    }

    @Scheduled(fixedDelay = 100)
    public void crawlerEUW(){
        tick(RegionType.EUW);
    }

    private void tick(RegionType region) {
        Optional<CrawlJob> job = crawlJobRepository.claimNextJob(region.getPlatform());
        if (job.isPresent()) {
            CrawlJob claimed = job.get();
            try {

                log.info("Claimed job for puuid {}, region {}", claimed.getPuuid(), region.name());
                Optional<Player> account = playerRepository.findByPuuid(claimed.getPuuid());
                if (account.isEmpty()) {
                    account = Optional.of(new Player(claimed.getPuuid()));
                }
                OffsetDateTime accountMustUpdateTime = account.get().getProfileUpdatedAt();
                if (accountMustUpdateTime == null || claimed.getPriority() >= 2 || accountMustUpdateTime.isBefore(OffsetDateTime.now().minusDays(profileTTLInDays))) {
                    Optional<AccountDto> accountByRiot = riotApiClient.getAccountByPuuid(claimed.getPuuid());
                    if (accountByRiot.isEmpty()) {
                        claimed.setRetryCounter(claimed.getRetryCounter() + 1);
                        if (claimed.getRetryCounter() >= maxRetries){
                            claimed.setStatus(CrawlStatus.ERROR);
                        } else {
                            claimed.setStatus(CrawlStatus.PENDING);
                        }
                        crawlJobRepository.save(claimed);
                        return;
                    }
                    Player finalAccount = account.get();
                    Optional<SummonerDto> summonerInfo = riotApiClient.getSummonerByPuuid(finalAccount.getPlatform() ,claimed.getPuuid());
                    finalAccount.setGameName(accountByRiot.get().gameName());
                    finalAccount.setTagLine(accountByRiot.get().tagLine());
                    summonerInfo.ifPresent(info -> {
                        finalAccount.setProfileIconId(info.profileIconId());
                        finalAccount.setSummonerLevel((int) info.summonerLevel());
                    });
                    finalAccount.setProfileUpdatedAt(OffsetDateTime.now());
                    playerRepository.save(finalAccount);

                    Set<LeagueEntryDto> leaguesFromRiot = riotApiClient.getLeagueByPuuid(claimed.getPuuid(), finalAccount.getPlatform());

                    for (LeagueEntryDto riotEntry : leaguesFromRiot) {
                        playerRankRepository.upsertPlayerRank(claimed.getPuuid(), riotEntry.queueType(), riotEntry.tier(),
                                riotEntry.rank(), riotEntry.leaguePoints(), riotEntry.wins(), riotEntry.losses());
                    }
                }
                List<String> matchList;
                int count = claimed.getLastCrawledAt() == null ? NEW_CRAWL_MATCH_LIST : RE_CRAWL_MATCH_LIST;
                matchList = riotApiClient.getMatchListByPuuid(claimed.getPuuid(), count, OffsetDateTime.now().minusDays(MATCH_LIST_START_TIME).toEpochSecond());
                matchList = matchList.stream()
                        .filter(id -> RegionType.fromSymbol(id.split("_")[0]).isPresent())
                        .toList();
                List<Match> existMatchList = matchRepository.findByMatchIdIn(matchList);
                Set<String> existingMatchIds = existMatchList.stream()
                        .map(Match::getMatchId)
                        .collect(Collectors.toSet());
                List<String> newMatchIdList = matchList.stream()
                        .filter(matchId -> !existingMatchIds.contains(matchId))
                        .toList().reversed();

                List<Future<MatchDto>> matchFutureList = new ArrayList<>();
                for (String matchId: newMatchIdList){
                    matchFutureList.add(matchExecutorService.submit(() -> riotApiClient.getMatchByMatchId(matchId)));
                }
                for (int i = 0; i < matchFutureList.size(); i++) {
                    try {
                        matchIngestService.ingest(matchFutureList.get(i).get());
                    } catch (InterruptedException e) {
                        log.error("Match: {} letöltése sikertelen", newMatchIdList.get(i), e);
                        Thread.currentThread().interrupt();
                        return;
                    } catch (ExecutionException e) {
                        log.warn("Match: {} letöltése sikertelen", newMatchIdList.get(i), e);
                    }
                }

                claimed.setLastCrawledAt(OffsetDateTime.now());
                claimed.setStatus(CrawlStatus.DONE);
                claimed.setPriority(0);
                claimed.setRetryCounter(0);
                crawlJobRepository.save(claimed);
            } catch (ResourceAccessException | HttpServerErrorException error) {
                log.error("Error for {}", claimed.getPuuid(), error);
                claimed.setStatus(CrawlStatus.PENDING);
                crawlJobRepository.save(claimed);
            } catch (HttpClientErrorException error) {
                if (error.getStatusCode() == HttpStatus.NOT_FOUND || error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    claimed.setRetryCounter(claimed.getRetryCounter() + 1);
                }
                if (error.getStatusCode() == HttpStatus.NOT_FOUND){
                    log.warn("Error for {}", claimed.getPuuid());
                } else {
                    log.error("Error for {}", claimed.getPuuid(), error);
                }
                if (claimed.getRetryCounter() >= maxRetries) {
                    claimed.setStatus(CrawlStatus.ERROR);
                } else {
                    claimed.setStatus(CrawlStatus.PENDING);
                }
                crawlJobRepository.save(claimed);
            }
        }
    }

    @Transactional
    @Scheduled(fixedDelayString = "${crawler.recrawl.interval-ms}")
    public void scheduleRecrawl() {
        int result = crawlJobRepository.scheduleRecrawl(OffsetDateTime.now().minusDays(recrawlTTLInDays), batchSize);
        if (result > 0) {
            log.info("Reset {} stale crawl jobs to new crawling", result);
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 5_000)
    public void recovery() {
        int result = crawlJobRepository.recovery(OffsetDateTime.now().minusMinutes(15), maxRetries);
        if (result > 0) {
            log.warn("Recovered {} stale crawl jobs stuck in IN_PROGRESS", result);
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void backfillPlayerRanksEUNE() {
        processPlayerRanks(RegionType.EUNE);
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void backfillPlayerRanksEUW() {
        processPlayerRanks(RegionType.EUW);
    }

    private void processPlayerRanks(RegionType region) {
        String platform = region.getPlatform();
        List<String> players = playerRepository.findTopUnrankedPuuids(platform, OffsetDateTime.now().minusDays(30), 1000);

        if (players.isEmpty()) {
            return;
        }

        List<String> donePuuids = new ArrayList<>();
        List<Future<Set<LeagueEntryDto>>> leagueEntryFutures = new ArrayList<>();

        for (String puuid : players) {
            leagueEntryFutures.add(rankExecutorService.submit(() ->
                    riotApiClient.getLeagueByPuuid(puuid, platform)
            ));
        }

        for (int i = 0; i < leagueEntryFutures.size(); i++) {
            String currentPuuid = players.get(i);
            try {
                Set<LeagueEntryDto> leagues = leagueEntryFutures.get(i).get();

                for (LeagueEntryDto league : leagues) {
                    playerRankRepository.upsertPlayerRank(
                            currentPuuid, league.queueType(), league.tier(),
                            league.rank(), league.leaguePoints(), league.wins(), league.losses()
                    );
                }
                donePuuids.add(currentPuuid);

            } catch (ExecutionException e) {
                log.error("ExecutionException for puuid: {}", currentPuuid, e);
            } catch (InterruptedException e) {
                log.error("InterruptedException while waiting for player ranks, aborting...", e);
                Thread.currentThread().interrupt();
                break;
            }
        }

        int updatedCount = 0;
        if (!donePuuids.isEmpty()) {
            updatedCount = playerRepository.saveRankCheck(donePuuids);
        }

        log.info("{} player ranks updated in {}", updatedCount, region.name());
    }
}
