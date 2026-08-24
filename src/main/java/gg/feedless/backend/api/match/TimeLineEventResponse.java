package gg.feedless.backend.api.match;

public record TimeLineEventResponse(int atSeconds, String type, String team, String actorChampionKey,
                                    String victimChampionKey, String detail, boolean major, int goldDiff) {
}
