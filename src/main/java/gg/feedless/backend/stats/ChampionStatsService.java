package gg.feedless.backend.stats;

import gg.feedless.backend.api.championstats.ChampionStatsListResponse;
import gg.feedless.backend.api.championstats.ChampionStatsResponse;
import gg.feedless.backend.api.championstats.FeaturedChampionResponse;
import gg.feedless.backend.api.championstats.Scope;
import gg.feedless.backend.match.MatchRepository;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ChampionStatsService {
    private final ChampionStatsRepository championStatsRepository;
    private final MatchRepository matchRepository;
    private final ChampionCatalog championCatalog;

    private static final int DEFAULT_MIN_GAMES = 50;

    private static final double MIN_FEATURED_PICK_RATE = 2.0;

    public ChampionStatsService(ChampionStatsRepository championStatsRepository, MatchRepository matchRepository, ChampionCatalog championCatalog) {
        this.championStatsRepository = championStatsRepository;
        this.matchRepository = matchRepository;
        this.championCatalog = championCatalog;
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

        List<ChampionStatsView> rows = championStatsRepository.getChampionStats(patch, queueId, tiers, minGames, region.getPlatform());

        int totalChamps = championStatsRepository.getTotalChampion(patch, queueId, region.getPlatform());

        Long updatedMinutesAgo = championStatsRepository.getLastUpdatedAt(patch,
                        queueId, region.getPlatform())
                .map(t -> Duration.between(t, Instant.now()).toMinutes())
                .orElse(null);

        List<ChampionStatsResponse> championStatsResponses = rows.stream().map(view ->
                new ChampionStatsResponse(championCatalog.getChampionKey(view.getChampionId()), toApiRole(view.getTeamPosition()),
                        null, view.getWinRate(), view.getPickRate(), null, view.getKda(), null,
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

        List<ChampionStatsView> rows = championStatsRepository.getChampionStats(patch, queueId, tiers, minGames, region.getPlatform());

        List<ChampionStatsView> sorted = rows.stream()
                .filter(view -> view.getPickRate() >= MIN_FEATURED_PICK_RATE)
                .sorted(Comparator.comparingDouble(ChampionStatsView::getWinRate).reversed())
                .toList();

        List<FeaturedChampionResponse> result = new ArrayList<>();

        for (int i = 0; i < Math.min(4, sorted.size()); i++) {
            result.add(new FeaturedChampionResponse(championCatalog.getChampionKey(sorted.get(i).getChampionId()), toApiRole(sorted.get(i).getTeamPosition()), i+1, sorted.get(i).getWinRate(), sorted.get(i).getPickRate()));
        }

        return result;
    }

    private static String toApiRole(String teamPosition) {
        return switch (teamPosition) {
            case "TOP" -> "Top";
            case "JUNGLE" -> "Jungle";
            case "BOTTOM" -> "Bot";
            case "UTILITY" -> "Support";
            default -> "Mid";
        };
    }
}
