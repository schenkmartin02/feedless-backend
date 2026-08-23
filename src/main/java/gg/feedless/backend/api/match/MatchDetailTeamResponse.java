package gg.feedless.backend.api.match;

import java.util.List;

public record MatchDetailTeamResponse(String side, boolean win, ObjectivesResponse objectives, List<MatchDetailPlayerResponse> players) {
}
