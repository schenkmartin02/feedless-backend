package gg.feedless.backend.riot.dto.spectator;

import java.util.List;

public record CurrentGameDto(long gameLength, int gameQueueConfigId, int mapId, List<CurrentGameParticipantDto> participants,
                             List<BannedChampionDto> bannedChampions) {
}
