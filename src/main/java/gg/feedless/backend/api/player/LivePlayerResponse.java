package gg.feedless.backend.api.player;

public record LivePlayerResponse(String name, String tag, String championKey, String rank, double winRate, int games, boolean isSubject) {
}
