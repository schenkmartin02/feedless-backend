package gg.feedless.backend.ladder;

import gg.feedless.backend.api.ladder.LadderEntryResponse;
import gg.feedless.backend.api.ladder.LadderResponse;
import gg.feedless.backend.player.PlayerRankRepository;
import gg.feedless.backend.riot.ApexTier;
import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import gg.feedless.backend.riot.dto.league.LeagueItemDto;
import gg.feedless.backend.riot.dto.league.LeagueListDto;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RegionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LadderService {
    private static final Logger log = LoggerFactory.getLogger(LadderService.class);

    private final LadderWrite ladderWrite;
    private final RiotApiClient riotApiClient;
    private final RankLeaderboardRepository rankLeaderboardRepository;
    private final PlayerRankRepository playerRankRepository;
    private final ChampionCatalog championCatalog;

    private final static int PAGE_SIZE = 100;
    private final static int MAX_POSITION = 1000;

    public LadderService(LadderWrite ladderWrite, RiotApiClient riotApiClient,
                         RankLeaderboardRepository rankLeaderboardRepository, PlayerRankRepository playerRankRepository,
                         ChampionCatalog championCatalog) {
        this.ladderWrite = ladderWrite;
        this.riotApiClient = riotApiClient;
        this.rankLeaderboardRepository = rankLeaderboardRepository;
        this.playerRankRepository = playerRankRepository;
        this.championCatalog = championCatalog;
    }

    @Scheduled(fixedDelay = 600_000)
    public void recomputeRankLadder(){
        for(RegionType region: RegionType.values()){
            for(QueueType queue: QueueType.values()){
                if (queue.getLeagueQueue() == null) continue;
                try {
                    List<Entry> entries = new ArrayList<>();
                    for(ApexTier tier: ApexTier.values()){
                        Optional<LeagueListDto> leagueBoard = riotApiClient.getLeaderboardLeagues(queue, region.getPlatform(), tier);
                        if (leagueBoard.isEmpty()) continue;
                        LeagueListDto leagueList = leagueBoard.get();
                        for (LeagueItemDto item: leagueList.entries()){
                            entries.add(new Entry(item, leagueList.tier()));
                        }
                    }
                    entries.sort(Comparator.comparingInt((Entry e) -> e.item().leaguePoints()).reversed().thenComparing(Comparator.comparingInt((Entry e) -> e.item().wins()).reversed()).thenComparing((Entry e) -> e.item().puuid()));
                    List<RankLeaderboard> leaderboard = new ArrayList<>();
                    for (int i = 0; i < entries.size(); i++) {
                        Entry entry = entries.get(i);
                        RankLeaderboard rank = new RankLeaderboard(region.getPlatform(), queue.getLeagueQueue(), i + 1,
                                entry.item.puuid(), entry.tier(), entry.item().leaguePoints(), entry.item.wins(),
                                entry.item.losses(), null, null, null);
                        leaderboard.add(rank);
                    }
                    ladderWrite.replace(region.getPlatform(), queue.getLeagueQueue(), leaderboard);
                    ladderWrite.enrich(region.getPlatform(), queue.getLeagueQueue(), queue.getQueue(), MAX_POSITION);
                } catch (Exception e) {
                    log.error("LadderService error: ", e);
                }
            }
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 120_000)
    public void updateRankedPlayerCount(){
        int result = playerRankRepository.recomputeRankedPlayerCount();
        log.info("Ranked player count is updated: {}", result);
    }

    @Transactional
    @Scheduled(fixedDelay = 86_400_000, initialDelay = 60_000)
    public void updateLadderSnapshot() {
        int insert = rankLeaderboardRepository.insertLadderSnapshot(LocalDate.now());
        int delete = rankLeaderboardRepository.deleteOldLadderSnapshot(LocalDate.now().minusDays(7));
        log.info("LadderSnapshot updated insert: {} and delete: {}", insert, delete);
    }

    public LadderResponse getLadder(QueueType queue, RegionType region, int page, String tier, String q) {
        int offset = (page - 1) * 100;
        if (tier != null) {
            tier = tier.toUpperCase();
        }
        long totalPlayers = playerRankRepository.getRankedPlayerCount(queue.getLeagueQueue(), region.getPlatform());
        long totalPagesNumber = rankLeaderboardRepository.countRankLadder(region.getPlatform(), queue.getLeagueQueue(), MAX_POSITION, tier,  q);
        List<LadderEntryView> ladderEntryViewList = rankLeaderboardRepository.getRankLadder(region.getPlatform(), queue.getLeagueQueue(), MAX_POSITION, PAGE_SIZE, offset, tier, q);
        LadderProjection projection = rankLeaderboardRepository.getResponseHeader(region.getPlatform(), queue.getLeagueQueue());
        int totalPages = (int) (totalPagesNumber + PAGE_SIZE - 1) / PAGE_SIZE;
        Instant updatedAt = projection.getUpdatedAt();
        Long updatedMinutesAgo = null;
        if (updatedAt != null) {
            updatedMinutesAgo = Duration.between(updatedAt, Instant.now()).toMinutes();
        }
        List<LadderEntryResponse> ladderEntryResponseList = new ArrayList<>();
        for (LadderEntryView view: ladderEntryViewList){
            List<String> topChampionKeys = new ArrayList<>();
            if (view.getTopChampionIds() != null) {
                for(Integer id: view.getTopChampionIds()) {
                    String championKey = championCatalog.getChampionKey(id);
                    if (championKey != null) {
                        topChampionKeys.add(championKey);
                    }
                }
            }
            String tierView = view.getTier().charAt(0) + view.getTier().substring(1).toLowerCase();
            ladderEntryResponseList.add(new LadderEntryResponse(view.getPosition(), view.getName(), view.getTag(), region.name(), view.getProfileIconId(), tierView, view.getLp(), view.getWins(), view.getLosses(), view.getKda(), view.getDelta(), topChampionKeys));
        }

        return new LadderResponse(queue.name().toLowerCase(), region.name(), page, totalPages, totalPlayers, projection.getChallengerCutoff(), updatedMinutesAgo, ladderEntryResponseList);

    }

    private record Entry(LeagueItemDto item, String tier) {}

}