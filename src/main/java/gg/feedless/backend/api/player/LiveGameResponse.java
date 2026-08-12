package gg.feedless.backend.api.player;

import java.util.List;

public record LiveGameResponse(String queue, String map, int elapsedSeconds, List<LiveTeamResponse> teams) {
}
