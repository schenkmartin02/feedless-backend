package gg.feedless.backend.api.player;

import java.util.List;

public record LiveTeamResponse(String side, List<String> banChampionKeys, List<LivePlayerResponse> players) {
}
