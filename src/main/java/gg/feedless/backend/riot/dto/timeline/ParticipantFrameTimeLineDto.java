package gg.feedless.backend.riot.dto.timeline;

public record ParticipantFrameTimeLineDto(int participantId, int totalGold, int xp, int minionsKilled,
                                          int jungleMinionsKilled, DamageStatsTimeLineDto damageStats) {
}
