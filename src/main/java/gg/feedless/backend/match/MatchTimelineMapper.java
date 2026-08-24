package gg.feedless.backend.match;

import gg.feedless.backend.api.match.*;
import gg.feedless.backend.riot.dto.timeline.*;

import java.util.*;

public class MatchTimelineMapper {
    private MatchTimelineMapper() {}

    private static String idToTeam(int participantId){
        return switch (participantId) {
            case 1, 2, 3, 4, 5 -> "blue";
            case 6, 7, 8, 9, 10 -> "red";
            default -> throw new IllegalStateException("Unexpected value: " + participantId);
        };
    }

    private static Map<Integer, String> participantsListToPuuidMap(List<ParticipantTimeLineDto> participants){
        HashMap<Integer, String> map = new HashMap<>();
        for (ParticipantTimeLineDto dto: participants){
            map.put(dto.participantId(), dto.puuid());
        }
        return map;
    }

    private static SeriesResponse framesToSeries(List<FramesTimeLineDto> frames){
        List<Integer> blueGold = new ArrayList<>();
        List<Integer> redGold = new ArrayList<>();
        List<Integer> blueXp = new ArrayList<>();
        List<Integer> redXp = new ArrayList<>();
        List<Integer> blueCs = new ArrayList<>();
        List<Integer> redCs = new ArrayList<>();
        List<Integer> blueDamage = new ArrayList<>();
        List<Integer> redDamage = new ArrayList<>();
        for(FramesTimeLineDto frame: frames){
            int blueSumGold = 0;
            int redSumGold = 0;
            int blueSumXp = 0;
            int redSumXp = 0;
            int blueSumCs = 0;
            int redSumCs = 0;
            int blueSumDamage = 0;
            int redSumDamage = 0;
            for(ParticipantFrameTimeLineDto pf: frame.participantFrames().values()){
                boolean blueSide = idToTeam(pf.participantId()).equals("blue");
                if (blueSide) {
                    blueSumGold += pf.totalGold();
                    blueSumXp += pf.xp();
                    blueSumCs += pf.minionsKilled() + pf.jungleMinionsKilled();
                    blueSumDamage += pf.damageStats().totalDamageDoneToChampions();
                }
                if (!blueSide) {
                    redSumGold += pf.totalGold();
                    redSumXp += pf.xp();
                    redSumCs += pf.minionsKilled() + pf.jungleMinionsKilled();
                    redSumDamage += pf.damageStats().totalDamageDoneToChampions();
                }
            }
            blueGold.add(blueSumGold);
            redGold.add(redSumGold);
            blueXp.add(blueSumXp);
            redXp.add(redSumXp);
            blueCs.add(blueSumCs);
            redCs.add(redSumCs);
            blueDamage.add(blueSumDamage);
            redDamage.add(redSumDamage);
        }
        TeamSeriesResponse gold = new TeamSeriesResponse(blueGold, redGold);
        TeamSeriesResponse xp = new TeamSeriesResponse(blueXp, redXp);
        TeamSeriesResponse cs = new TeamSeriesResponse(blueCs, redCs);
        TeamSeriesResponse damage = new TeamSeriesResponse(blueDamage, redDamage);
        return new SeriesResponse(gold, damage, xp, cs);
    }

    private static List<Integer> goldDiffCalculate(TeamSeriesResponse goldTeamSeriesResponse){
        List<Integer> diff = new ArrayList<>();
        for (int i = 0; i < goldTeamSeriesResponse.blue().size(); i++) {
            diff.add(goldTeamSeriesResponse.blue().get(i) - goldTeamSeriesResponse.red().get(i));
        }
        return diff;
    }

    private static List<Integer> killDiffCalculate(List<FramesTimeLineDto> frames){
        List<Integer> killDiff = new ArrayList<>();
        int blueKills = 0;
        int redKills = 0;
        for(FramesTimeLineDto frame: frames){
            for(EventsTimeLineDto dto: frame.events()){
                if (!Objects.equals(dto.type(), "CHAMPION_KILL")) continue;
                if (dto.killerId() == null) continue;
                if (dto.killerId() == 0) continue;
                boolean blueSide = idToTeam(dto.killerId()).equals("blue");
                if (blueSide){
                    blueKills += 1;
                } else {
                    redKills += 1;
                }
            }
            killDiff.add(blueKills-redKills);
        }
        return killDiff;
    }

    private static List<TimeLineEventResponse> frameToEvents(List<FramesTimeLineDto> frames, Map<Integer, String> championKeyByParticipantId, List<Integer> goldDiff){
        List<TimeLineEventResponse> timeLineEventResponseList = new ArrayList<>();
        boolean first = true;
        for(FramesTimeLineDto frame: frames){
            for(EventsTimeLineDto dto: frame.events()){
                switch (dto.type()){
                    case "CHAMPION_KILL" -> {
                        if (dto.killerId() == null) continue;
                        if (dto.killerId() == 0) continue;
                        String type = first ? "FIRST_BLOOD" : "KILL";
                        boolean major = false;
                        if (first){
                            first = false;
                            major = true;
                        }
                        timeLineEventResponseList.add(new TimeLineEventResponse(timestampToSeconds(dto.timestamp()), type, idToTeam(dto.killerId()), championKeyByParticipantId.get(dto.killerId()), championKeyByParticipantId.get(dto.victimId()), null, major, goldDiffBySnapshot(dto.timestamp(), goldDiff)));
                    }
                    case "BUILDING_KILL" -> {
                        String type = Objects.equals(dto.buildingType(), "TOWER_BUILDING") ? "TOWER" : "INHIBITOR";
                        String team = dto.teamId() == 200 ? "blue" : "red";
                        String detail = dto.laneType().split("_")[0];
                        boolean major = type.equals("INHIBITOR");
                        timeLineEventResponseList.add(new TimeLineEventResponse(timestampToSeconds(dto.timestamp()), type, team, null, null, detail, major, goldDiffBySnapshot(dto.timestamp(), goldDiff)));
                    }
                    case "ELITE_MONSTER_KILL" -> {
                        String type = null;
                        String detail = null;
                        boolean major = false;
                        switch (dto.monsterType()){
                            case "DRAGON" -> {
                                type = "DRAGON";
                                detail = dto.monsterSubType();
                            }
                            case "RIFTHERALD" -> {
                                type = "HERALD";
                            }
                            case "BARON_NASHOR" -> {
                                type = "BARON";
                                major = true;
                            }
                            case "HORDE" -> {
                                type = "VOIDGRUB";
                            }
                        }
                        if (type == null) continue;
                        String team = dto.killerTeamId() == 100 ? "blue" : "red";
                        timeLineEventResponseList.add(new TimeLineEventResponse(timestampToSeconds(dto.timestamp()), type, team, null, null, detail, major, goldDiffBySnapshot(dto.timestamp(), goldDiff)));
                    }
                    case "CHAMPION_SPECIAL_KILL" -> {
                        if (!Objects.equals(dto.killType(), "KILL_ACE")) continue;
                        timeLineEventResponseList.add(new TimeLineEventResponse(timestampToSeconds(dto.timestamp()), "ACE", idToTeam(dto.killerId()), null, null, null, true, goldDiffBySnapshot(dto.timestamp(), goldDiff)));
                    }
                    case "DRAGON_SOUL_GIVEN" -> {
                        if (dto.teamId() == 0) continue;
                        String team = dto.teamId() == 100? "blue" : "red";
                        timeLineEventResponseList.add(new TimeLineEventResponse(timestampToSeconds(dto.timestamp()), "DRAGON_SOUL", team, null, null, dto.name(), true, goldDiffBySnapshot(dto.timestamp(), goldDiff)));
                    }
                    case "GAME_END" -> {
                        String team = dto.winningTeam() == 100 ? "blue" : "red";
                        timeLineEventResponseList.add(new TimeLineEventResponse(timestampToSeconds(dto.timestamp()), "NEXUS", team, null, null, null, true, goldDiffBySnapshot(dto.timestamp(), goldDiff)));
                    }
                }
            }
        }
        return timeLineEventResponseList;
    }

    private static List<FirstResponse> framesToFirst(List<TimeLineEventResponse> events){
        List<FirstResponse> responseList = new ArrayList<>();
        List<String> typeList = List.of("FIRST_BLOOD", "DRAGON", "HERALD", "TOWER", "BARON");
        for(String type: typeList){
            for(TimeLineEventResponse event: events){
                if (!Objects.equals(event.type(), type)) continue;
                responseList.add(new FirstResponse(type, event.atSeconds(), event.team(), event.actorChampionKey()));
                break;
            }
        }
        return responseList;
    }

    private static Map<String, List<PurchaseResponse>> framesToPurchase(Map<Integer, String> puuidByParticipantId, List<FramesTimeLineDto> frames){
        Map<String, List<PurchaseResponse>> result = new HashMap<>();
        for(FramesTimeLineDto frame: frames) {
            for (EventsTimeLineDto event : frame.events()) {
                if (!Objects.equals(event.type(), "ITEM_PURCHASED")) continue;
                String puuid = puuidByParticipantId.get(event.participantId());
                if (puuid == null) continue;
                result.computeIfAbsent(puuid, k -> new ArrayList<>()).add(new PurchaseResponse(timestampToSeconds(event.timestamp()), event.itemId()));
            }
        }
        return result;
    }

    private static Map<String, List<String>> framesToSkillOrder(Map<Integer, String> puuidByParticipantId, List<FramesTimeLineDto> frames){
        Map<String, List<String>> result = new HashMap<>();
        for(FramesTimeLineDto frame: frames) {
            for (EventsTimeLineDto event : frame.events()) {
                if (!Objects.equals(event.type(), "SKILL_LEVEL_UP")) continue;
                if (!Objects.equals(event.levelUpType(), "NORMAL")) continue;
                String puuid = puuidByParticipantId.get(event.participantId());
                if (puuid == null) continue;
                String skill = switch (event.skillSlot()){
                    case 1 -> "Q";
                    case 2 -> "W";
                    case 3 -> "E";
                    case 4 -> "R";
                    default -> "skip";
                };
                if (skill.equals("skip")) continue;
                result.computeIfAbsent(puuid, k -> new ArrayList<>()).add(skill);
            }
        }
        return result;
    }

    public static MatchTimelinePayload toPayload(TimelineDto dto, Map<String, String> championKeyByPuuid){
        Map<Integer, String> puuidByParticipantId = participantsListToPuuidMap(dto.info().participants());
        Map<Integer, String> championKeyByParticipantId = new HashMap<>();
        for (Map.Entry<Integer, String> entry: puuidByParticipantId.entrySet()){
            String puuid = entry.getValue();
            championKeyByParticipantId.put(entry.getKey(), championKeyByPuuid.get(puuid));
        }
        SeriesResponse series = framesToSeries(dto.info().frames());
        List<Integer> goldDiff = goldDiffCalculate(series.gold());
        List<Integer> killDiff = killDiffCalculate(dto.info().frames());
        List<TimeLineEventResponse> events = frameToEvents(dto.info().frames(), championKeyByParticipantId, goldDiff);
        List<FirstResponse> firsts = framesToFirst(events);
        Map<String, List<PurchaseResponse>> purchases = framesToPurchase(puuidByParticipantId, dto.info().frames());
        Map<String, List<String>> skillOrder = framesToSkillOrder(puuidByParticipantId, dto.info().frames());
        return new MatchTimelinePayload(new TimelineResponse(goldDiff, killDiff, series, events, firsts), purchases, skillOrder);
    }

    private static int timestampToSeconds(long timestamp){
        return Math.toIntExact(timestamp / 1000);
    }

    private static int goldDiffBySnapshot(long timestamp, List<Integer> goldDiff){
        int minutes = Math.toIntExact(timestamp / 60000);
        int goldDiffSize = goldDiff.size();
        if (goldDiff.isEmpty()){
            return 0;
        }
        if (minutes < goldDiffSize){
            return goldDiff.get(minutes);
        }

        return goldDiff.getLast();
    }
}
