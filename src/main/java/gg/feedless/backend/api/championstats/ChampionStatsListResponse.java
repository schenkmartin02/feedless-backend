package gg.feedless.backend.api.championstats;

import java.util.List;

public record ChampionStatsListResponse(Scope scope, String patch, int totalChampions, Long updatedMinutesAgo, List<ChampionStatsResponse> rows) {
}
