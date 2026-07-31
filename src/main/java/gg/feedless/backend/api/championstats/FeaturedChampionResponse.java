package gg.feedless.backend.api.championstats;

public record FeaturedChampionResponse(String championKey, String role, int rank, double winRate, double pickRate) {
}
