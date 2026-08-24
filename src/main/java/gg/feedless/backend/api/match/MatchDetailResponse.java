package gg.feedless.backend.api.match;

import java.time.Instant;
import java.util.List;

public record MatchDetailResponse(String id, String queue, String patch, Instant playedAt, int playedMinutesAgo,
                                  int durationSeconds, String winner, SubjectResponse subject, Integer lp,
                                  List<MatchDetailTeamResponse> teams, TimelineResponse timeline, MatchBuildResponse build) {
}
