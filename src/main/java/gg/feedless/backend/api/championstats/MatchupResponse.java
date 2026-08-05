package gg.feedless.backend.api.championstats;

public record MatchupResponse(String championKey, double winRate, long games) {
}
