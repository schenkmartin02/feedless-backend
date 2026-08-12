package gg.feedless.backend.api.player;

import java.util.List;

public record MatchResponse (String id, String championKey, boolean win, int kills, int deaths, int assists, int cs,
                             double csPerMin, double goldPerMin, int level, String queue, int durationSeconds,
                             int playedMinutesAgo, Integer lp, List<String> badges, List<Integer> itemIds,
                             List<MatchPlayerResponse> blueTeam, List<MatchPlayerResponse> redTeam){
}
