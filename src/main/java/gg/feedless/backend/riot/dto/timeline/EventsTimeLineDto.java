package gg.feedless.backend.riot.dto.timeline;

public record EventsTimeLineDto(long timestamp, String type,
                                // CHAMPION_KILL, CHAMPION_SPECIAL_KILL, BUILDING_KILL, ELITE_MONSTER_KILL
                                Integer killerId, Integer victimId,
                                // CHAMPION_SPECIAL_KILL: KILL_FIRST_BLOOD | KILL_ACE | KILL_MULTI
                                String killType,
                                // BUILDING_KILL - a teamId a VESZTES csapat, nem a romboló
                                Integer teamId, String buildingType, String laneType, String towerType,
                                // ELITE_MONSTER_KILL - itt a killerTeamId tényleg a szerző
                                Integer killerTeamId, String monsterType, String monsterSubType,
                                // DRAGON_SOUL_GIVEN
                                String name,
                                // ITEM_PURCHASED, SKILL_LEVEL_UP
                                Integer participantId, Integer itemId, Integer skillSlot, Integer winningTeam, String levelUpType) {
}
