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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private final ExecutorService executorService;

    private final int profileTTLInDays;
    private final int recrawlTTLInDays;
    private final int batchSize;

    private static final int NEW_CRAWL_MATCH_LIST = 20;
    private static final int RE_CRAWL_MATCH_LIST = 100;
    private static final int MATCH_LIST_START_TIME = 30;

    public CrawlWorker(CrawlJobRepository crawlJobRepository, RiotApiClient riotApiClient, PlayerRepository playerRepository, PlayerRankRepository playerRankRepository, MatchRepository matchRepository, MatchIngestService matchIngestService, @Value("${crawler.profile-refresh-ttl-days}") int profileTTLInDays, @Value("${crawler.recrawl.ttl-days}") int recrawlTTLInDays, @Value("${crawler.recrawl.batch-size}") int batchSize, ExecutorService executorService) {
        this.crawlJobRepository = crawlJobRepository;
        this.riotApiClient = riotApiClient;
        this.playerRepository = playerRepository;
        this.playerRankRepository = playerRankRepository;
        this.matchRepository = matchRepository;
        this.matchIngestService = matchIngestService;
        this.profileTTLInDays = profileTTLInDays;
        this.recrawlTTLInDays = recrawlTTLInDays;
        this.batchSize = batchSize;
        this.executorService = executorService;
    }

    @Scheduled(fixedDelay = 100)
    public void tick() {
        Optional<CrawlJob> job = crawlJobRepository.claimNextJob();
        if (job.isPresent()) {
            CrawlJob claimed = job.get();
            log.info("Claimed job for puuid {}", claimed.getPuuid());
            Optional<Player> account = playerRepository.findByPuuid(claimed.getPuuid());
            if (account.isEmpty()) {
                account = Optional.of(new Player(claimed.getPuuid()));
            }
            OffsetDateTime accountMustUpdateTime = account.get().getProfileUpdatedAt();
            if (accountMustUpdateTime == null || claimed.getPriority() >= 2 || accountMustUpdateTime.isBefore(OffsetDateTime.now().minusDays(profileTTLInDays))) {
                Optional<AccountDto> accountByRiot = riotApiClient.getAccountByPuuid(claimed.getPuuid());
                if (accountByRiot.isEmpty()) {
                    if (claimed.getRetryCounter() >= 4) {
                        claimed.setStatus(CrawlStatus.ERROR);
                        crawlJobRepository.save(claimed);
                        return;
                    }
                    claimed.setRetryCounter(claimed.getRetryCounter() + 1);
                    claimed.setStatus(CrawlStatus.PENDING);
                    crawlJobRepository.save(claimed);
                    return;
                }
                SummonerDto summonerInfo = riotApiClient.getSummonerByPuuid("eune" ,claimed.getPuuid());
                account.get().setGameName(accountByRiot.get().gameName());
                account.get().setTagLine(accountByRiot.get().tagLine());
                account.get().setProfileIconId(summonerInfo.profileIconId());
                account.get().setProfileUpdatedAt(OffsetDateTime.now());
                playerRepository.save(account.get());

                Set<LeagueEntryDto> leaguesFromRiot = riotApiClient.getLeagueByPuuid(claimed.getPuuid());

                for (LeagueEntryDto riotEntry : leaguesFromRiot) {
                    playerRankRepository.upsertPlayerRank(claimed.getPuuid(), riotEntry.queueType(), riotEntry.tier(),
                            riotEntry.rank(), riotEntry.leaguePoints(), riotEntry.wins(), riotEntry.losses());
                }
            }
            List<String> matchList;
            int count = claimed.getLastCrawledAt() == null ? NEW_CRAWL_MATCH_LIST : RE_CRAWL_MATCH_LIST;
            matchList = riotApiClient.getMatchListByPuuid(claimed.getPuuid(), count, OffsetDateTime.now().minusDays(MATCH_LIST_START_TIME).toEpochSecond());
            List<Match> existMatchList = matchRepository.findByMatchIdIn(matchList);
            Set<String> existingMatchIds = existMatchList.stream()
                    .map(Match::getMatchId)
                    .collect(Collectors.toSet());
            List<String> newMatchIdList = matchList.stream()
                    .filter(matchId -> !existingMatchIds.contains(matchId))
                    .toList();

            List<Future<MatchDto>> matchFutureList = new ArrayList<>();
            for (String matchId: newMatchIdList){
                matchFutureList.add(executorService.submit(() -> riotApiClient.getMatchByMatchId(matchId)));
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
        int result = crawlJobRepository.recovery(OffsetDateTime.now().minusMinutes(15));
        if (result > 0) {
            log.warn("Recovered {} stale crawl jobs stuck in IN_PROGRESS", result);
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 100)
    public void backfillPlayerRanks() {
        List<String> players = playerRepository.findTopUnrankedPuuids("EUN1", OffsetDateTime.now().minusDays(30), 20);
        List<String> donePuuid = new ArrayList<>();
        for (String puuid : players) {
            try {
                Set<LeagueEntryDto> leagueEntryDtos = riotApiClient.getLeagueByPuuid(puuid);
                for (LeagueEntryDto league : leagueEntryDtos) {
                    playerRankRepository.upsertPlayerRank(puuid, league.queueType(), league.tier(), league.rank(), league.leaguePoints(), league.wins(), league.losses());
                }
                donePuuid.add(puuid);
            } catch (Exception e) {
                log.error("Hiba: {}", puuid, e);
            }
        }
        int puuids = 0;
        if (!donePuuid.isEmpty()) {
            puuids = playerRepository.saveRankCheck(donePuuid);
        }
        log.info("{} player ranks is up to date", puuids);
    }
}
