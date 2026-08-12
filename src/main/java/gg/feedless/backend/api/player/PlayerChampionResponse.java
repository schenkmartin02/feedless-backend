package gg.feedless.backend.api.player;

public record PlayerChampionResponse (String championKey, int games, double winRate, double kda, double kills,
                                      double deaths, double assists, double csPerMin, double goldPerMin) {
}
