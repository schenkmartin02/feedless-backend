package gg.feedless.backend.api.sitestats;

public record SiteStatsResponse(long analyzedMatches, long trackedPlayers, int championCount) {
}
