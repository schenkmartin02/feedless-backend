package gg.feedless.backend.api.player;

import gg.feedless.backend.riot.dto.spectator.LivePerks;

public record LivePlayerResponse(String name, String tag, int profileIconId, String championKey, int spell1Id, int spell2Id, LivePerks perks, int lastSelectedSkinIndex, String rank, double winRate, int games, boolean isSubject) {
}
