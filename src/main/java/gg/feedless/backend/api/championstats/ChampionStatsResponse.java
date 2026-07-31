package gg.feedless.backend.api.championstats;

public record ChampionStatsResponse(String championKey, String role, String tier, double winRate, double pickRate,
                                    Double banRate, double kda, Integer trend, long games,
                                    double csPerMin, int goldPerMin, double kills, double deaths, double assists) {
}
