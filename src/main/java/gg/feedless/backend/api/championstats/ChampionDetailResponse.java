package gg.feedless.backend.api.championstats;

import java.util.List;

public record ChampionDetailResponse(ChampionStatsResponse stats, Scope scope, String patch, String role,
                                     List<String> availableRoles, int roleRank, int rolePool, Double banDelta,
                                     BuildResponse build, RunesResponse runes, List<MatchupResponse> strongAgainst,
                                     List<MatchupResponse> weakAgainst) {
}
