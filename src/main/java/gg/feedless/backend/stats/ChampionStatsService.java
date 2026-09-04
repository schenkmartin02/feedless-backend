package gg.feedless.backend.stats;

import gg.feedless.backend.api.championstats.*;
import gg.feedless.backend.match.MatchRepository;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ChampionStatsService {
    private final ChampionStatsRepository championStatsRepository;
    private final MatchRepository matchRepository;
    private final ChampionCatalog championCatalog;
    private final MatchupStatsRepository matchupStatsRepository;
    private final RuneStatsRepository runeStatsRepository;
    private final ChampionBanSnapshotRepository championBanSnapshotRepository;

    public static final int DEFAULT_MIN_GAMES = 200;

    private static final double MIN_FEATURED_PICK_RATE = 2.0;

    private static final int MIN_MATCHUP_GAMES = 100;

    public ChampionStatsService(ChampionStatsRepository championStatsRepository, MatchRepository matchRepository, ChampionCatalog championCatalog, MatchupStatsRepository matchupStatsRepository, RuneStatsRepository runeStatsRepository, ChampionBanSnapshotRepository championBanSnapshotRepository) {
        this.championStatsRepository = championStatsRepository;
        this.matchRepository = matchRepository;
        this.championCatalog = championCatalog;
        this.matchupStatsRepository = matchupStatsRepository;
        this.runeStatsRepository = runeStatsRepository;
        this.championBanSnapshotRepository = championBanSnapshotRepository;
    }

    public ChampionStatsListResponse getChampionStats(QueueType queue, BracketType bracket, RegionType region, String patch, Integer minGames) {
        int queueId = queue.getQueue();
        Set<String> tiers = bracket.getTiers();
        Scope scope = new Scope(queue.name().toLowerCase(),
                bracket.name().toLowerCase(), region.name());
        if (minGames == null) {
            minGames = DEFAULT_MIN_GAMES;
        }
        if (patch == null) {
            Optional<String> newPatch = matchRepository.getLastPatch();
            if (newPatch.isEmpty()) {
                return new ChampionStatsListResponse(scope, null, 0, null, List.of());
            } else {
                patch = newPatch.get();
            }
        }

        List<ChampionStatsView> rows = championStatsRepository.getChampionStats(patch, queueId, tiers, minGames, region.getPlatform(), bracket.name().toLowerCase(), LocalDate.now().minusDays(1));

        int totalChamps = championStatsRepository.getTotalChampion(patch, queueId, region.getPlatform());

        Long updatedMinutesAgo = championStatsRepository.getLastUpdatedAt(patch,
                        queueId, region.getPlatform())
                .map(t -> Duration.between(t, Instant.now()).toMinutes())
                .orElse(null);

        List<ChampionStatsResponse> championStatsResponses = rows.stream().map(view ->
                new ChampionStatsResponse(championCatalog.getChampionKey(view.getChampionId()), RoleType.fromTeamPosition(view.getTeamPosition()).getRole(),
                        view.getTier(), view.getWinRate(), view.getPickRate(), view.getBanRate(), view.getKda(), view.getTrend(),
                        view.getGames(), view.getCsPerMinute(), view.getGoldPerMinute(), view.getAvgKills(),
                        view.getAvgDeaths(), view.getAvgAssists())).toList();

        return new ChampionStatsListResponse(scope, patch, totalChamps, updatedMinutesAgo, championStatsResponses);

    }

    public List<FeaturedChampionResponse> getFeaturedChampions(QueueType queue, BracketType bracket, RegionType region, String patch, Integer minGames) {
        int queueId = queue.getQueue();
        Set<String> tiers = bracket.getTiers();
        if (minGames == null) {
            minGames = DEFAULT_MIN_GAMES;
        }
        if (patch == null) {
            Optional<String> newPatch = matchRepository.getLastPatch();
            if (newPatch.isEmpty()) {
                return List.of();
            } else {
                patch = newPatch.get();
            }
        }

        List<ChampionStatsView> rows = championStatsRepository.getChampionStats(patch, queueId, tiers, minGames, region.getPlatform(), bracket.name().toLowerCase(), LocalDate.now().minusDays(1));

        List<ChampionStatsView> sorted = rows.stream()
                .filter(view -> view.getPickRate() >= MIN_FEATURED_PICK_RATE)
                .sorted(Comparator.comparingDouble(ChampionStatsView::getWinRate).reversed())
                .toList();

        List<FeaturedChampionResponse> result = new ArrayList<>();

        for (int i = 0; i < Math.min(4, sorted.size()); i++) {
            result.add(new FeaturedChampionResponse(championCatalog.getChampionKey(sorted.get(i).getChampionId()), RoleType.fromTeamPosition(sorted.get(i).getTeamPosition()).getRole(), i+1, sorted.get(i).getWinRate(), sorted.get(i).getPickRate()));
        }

        return result;
    }

    public Optional<ChampionDetailResponse> getChampionDetail(String championKey, QueueType queue, BracketType bracket, RegionType region, RoleType role, String patch, Integer minGames) {
        Integer championId = championCatalog.getChampionId(championKey);
        if (championId == null) {
            return Optional.empty();
        }
        Scope scope = new Scope(queue.name().toLowerCase(),
                bracket.name().toLowerCase(), region.name());
        int queueId = queue.getQueue();
        Set<String> tiers = bracket.getTiers();
        if (minGames == null) {
            minGames = DEFAULT_MIN_GAMES;
        }
        if (patch == null) {
            Optional<String> newPatch = matchRepository.getLastPatch();
            if (newPatch.isEmpty()) {
                return Optional.empty();
            } else {
                patch = newPatch.get();
            }
        }
        List<ChampionStatsView> rows = championStatsRepository.getChampionStats(patch, queueId, tiers, minGames, region.getPlatform(), bracket.name().toLowerCase(), LocalDate.now().minusDays(1));

        List<ChampionStatsView> championWithPositions = new ArrayList<>();
        for (ChampionStatsView view: rows) {
            if (Objects.equals(view.getChampionId(), championId)) {
                championWithPositions.add(view);
            }
        }
        if (championWithPositions.isEmpty()) {
            return Optional.empty();
        }

        List<String> availableRoles = new ArrayList<>();
        for (ChampionStatsView view: championWithPositions) {
            availableRoles.add(RoleType.fromTeamPosition(view.getTeamPosition()).getRole());
        }

        ChampionStatsView actualStats = null;
        if (role == null) {
            actualStats = championWithPositions.getFirst();
        } else {
            for (ChampionStatsView view: championWithPositions) {
                if (RoleType.fromTeamPosition(view.getTeamPosition()) == role) {
                    actualStats = view;
                }
            }
        }

        if (actualStats == null) {
            return Optional.empty();
        }

        String actualRole = RoleType.fromTeamPosition(actualStats.getTeamPosition()).getRole();

        ChampionStatsResponse championStatsResponse = new ChampionStatsResponse(championKey, actualRole,
                actualStats.getTier(), actualStats.getWinRate(), actualStats.getPickRate(), actualStats.getBanRate(),
                actualStats.getKda(), actualStats.getTrend(), actualStats.getGames(), actualStats.getCsPerMinute(),
                actualStats.getGoldPerMinute(), actualStats.getAvgKills(), actualStats.getAvgDeaths(),
                actualStats.getAvgAssists());

        List<MatchupStatsView> matchups = matchupStatsRepository.getMatchupStats(region.getPlatform(), patch, queueId, championId, actualStats.getTeamPosition(), tiers, MIN_MATCHUP_GAMES);

        int take = Math.min(10, matchups.size() / 2);

        List<MatchupStatsView> strongAgainst = matchups.subList(0, take);
        List<MatchupStatsView> weakAgainst = matchups.reversed().subList(0, take);

        List<MatchupResponse> strongAgainstResponse = new ArrayList<>();
        List<MatchupResponse> weakAgainstResponse = new ArrayList<>();
        for (MatchupStatsView view: strongAgainst) {
            strongAgainstResponse.add(new MatchupResponse(championCatalog.getChampionKey(view.getOpponentChampionId()), view.getWinRate(), view.getGames()));
        }
        for (MatchupStatsView view: weakAgainst) {
            weakAgainstResponse.add(new MatchupResponse(championCatalog.getChampionKey(view.getOpponentChampionId()), view.getWinRate(), view.getGames()));
        }

        RuneStatsView runeStatsView = runeStatsRepository.getRuneStats(region.getPlatform(), patch, queueId, championId, actualStats.getTeamPosition(), tiers);

        RunesResponse runes = null;
        if (runeStatsView != null) {
            runes = new RunesResponse(List.of(runeStatsView.getKeystoneId(), runeStatsView.getPrimaryPerk2(), runeStatsView.getPrimaryPerk3(), runeStatsView.getPrimaryPerk4()), List.of(runeStatsView.getSubPerk1(), runeStatsView.getSubPerk2()));
        }

        Optional<Double> banSnapshot = championBanSnapshotRepository.getPatchValue(region.getPlatform(), patch, queueId, bracket.name().toLowerCase(), championId);

        Double banDelta = null;
        if (banSnapshot.isPresent() && actualStats.getBanRate() != null) {
            banDelta =(Math.round((actualStats.getBanRate() - banSnapshot.get())*10.0)/10.0);
        }

        return Optional.of(new ChampionDetailResponse(championStatsResponse, scope, patch, actualRole, availableRoles, actualStats.getRoleRank(), actualStats.getRolePool(), banDelta, null, runes, strongAgainstResponse, weakAgainstResponse));
    }
}
