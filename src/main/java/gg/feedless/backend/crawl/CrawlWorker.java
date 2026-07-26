package gg.feedless.backend.crawl;

import gg.feedless.backend.match.Match;
import gg.feedless.backend.match.MatchRepository;
import gg.feedless.backend.player.Player;
import gg.feedless.backend.player.PlayerRank;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private final int profileTTLInDays;
    private final int recrawlTTLInDays;
    private final int batchSize;

    private static final int NEW_CRAWL_MATCH_LIST = 20;
    private static final int RE_CRAWL_MATCH_LIST = 100;

    public CrawlWorker(CrawlJobRepository crawlJobRepository, RiotApiClient riotApiClient, PlayerRepository playerRepository, PlayerRankRepository playerRankRepository, MatchRepository matchRepository, MatchIngestService matchIngestService, @Value("${crawler.profile-refresh-ttl-days}") int profileTTLInDays, @Value("${crawler.recrawl.ttl-days}") int recrawlTTLInDays, @Value("${crawler.recrawl.batch-size}") int batchSize) {
        this.crawlJobRepository = crawlJobRepository;
        this.riotApiClient = riotApiClient;
        this.playerRepository = playerRepository;
        this.playerRankRepository = playerRankRepository;
        this.matchRepository = matchRepository;
        this.matchIngestService = matchIngestService;
        this.profileTTLInDays = profileTTLInDays;
        this.recrawlTTLInDays = recrawlTTLInDays;
        this.batchSize = batchSize;
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
                SummonerDto summonerInfo = riotApiClient.getSummonerByPuuid(claimed.getPuuid());
                account.get().setGameName(accountByRiot.get().gameName());
                account.get().setTagLine(accountByRiot.get().tagLine());
                account.get().setProfileIconId(summonerInfo.profileIconId());
                account.get().setProfileUpdatedAt(OffsetDateTime.now());
                playerRepository.save(account.get());

                List<PlayerRank> leagues = playerRankRepository.findByPlayerId(account.get().getId());
                Set<LeagueEntryDto> leaguesFromRiot = riotApiClient.getLeagueByPuuid(claimed.getPuuid());

                for (LeagueEntryDto riotEntry : leaguesFromRiot) {

                    Optional<Player> finalAccount = account;
                    leagues.stream()
                            .filter(dbRank -> dbRank.getQueueType().equals(riotEntry.queueType()))
                            .findFirst()
                            .ifPresentOrElse(
                                    existingRank -> {
                                        existingRank.setTier(riotEntry.tier());
                                        existingRank.setDivision(riotEntry.rank());
                                        existingRank.setLeaguePoints(riotEntry.leaguePoints());
                                        existingRank.setWins(riotEntry.wins());
                                        existingRank.setLosses(riotEntry.losses());
                                        playerRankRepository.save(existingRank);
                                    },
                                    () -> {
                                        PlayerRank newRank = new PlayerRank(
                                                finalAccount.get().getId(),
                                                riotEntry.queueType(),
                                                riotEntry.tier(),
                                                riotEntry.rank(),
                                                riotEntry.leaguePoints(),
                                                riotEntry.wins(),
                                                riotEntry.losses()
                                        );
                                        playerRankRepository.save(newRank);
                                    }
                            );
                }
            }
            List<String> matchList;
            if (claimed.getLastCrawledAt() == null) {
                matchList = riotApiClient.getMatchListByPuuid(claimed.getPuuid(), NEW_CRAWL_MATCH_LIST);
            } else {
                matchList = riotApiClient.getMatchListByPuuid(claimed.getPuuid(), RE_CRAWL_MATCH_LIST);
            }
            List<Match> existMatchList = matchRepository.findByMatchIdIn(matchList);
            Set<String> existingMatchIds = existMatchList.stream()
                    .map(Match::getMatchId)
                    .collect(Collectors.toSet());
            List<String> newMatchIdList = matchList.stream()
                    .filter(matchId -> !existingMatchIds.contains(matchId))
                    .toList();

            for (String matchId: newMatchIdList){
                MatchDto match = riotApiClient.getMatchByMatchId(matchId);
                matchIngestService.ingest(match);
            }

            claimed.setLastCrawledAt(OffsetDateTime.now());
            claimed.setStatus(CrawlStatus.DONE);
            claimed.setPriority(0);
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
}
