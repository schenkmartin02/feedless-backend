package gg.feedless.backend.riot.dto.spectator;

public record CurrentGameParticipantDto(String puuid, int teamId, int championId, String riotId, int spell1Id,
                                        int spell2Id, int lastSelectedSkinIndex, LivePerks perks, int profileIconId) {
}
