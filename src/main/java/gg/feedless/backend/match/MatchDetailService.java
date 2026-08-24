package gg.feedless.backend.match;

import gg.feedless.backend.api.match.*;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import gg.feedless.backend.stats.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class MatchDetailService {
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final ChampionCatalog championCatalog;
    private final RuneStatsRepository runeStatsRepository;
    private final MatchTimelineService matchTimelineService;

    private static final int MIN_GAMES = 100;

    public MatchDetailService(MatchRepository matchRepository, ParticipantRepository participantRepository, MatchTeamRepository matchTeamRepository, ChampionCatalog championCatalog, RuneStatsRepository runeStatsRepository, MatchTimelineService matchTimelineService) {
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.championCatalog = championCatalog;
        this.runeStatsRepository = runeStatsRepository;
        this.matchTimelineService = matchTimelineService;
    }

    public Optional<MatchDetailResponse> getMatchDetail(String matchId, RegionType region, String name, String tag){
        Optional<Match> match = matchRepository.findByMatchId(matchId);
        if (match.isEmpty()) return Optional.empty();
        List<MatchDetailParticipantView> matchDetailParticipantViewsList = participantRepository.findMatchDetailParticipants(match.get().getId());
        if (matchDetailParticipantViewsList.isEmpty()) return Optional.empty();
        MatchDetailParticipantView subject = null;
        if (name != null && tag != null){
            for (MatchDetailParticipantView view: matchDetailParticipantViewsList){
                if (view.getName() != null && view.getTag() != null){
                    if (view.getName().equalsIgnoreCase(name) && view.getTag().equalsIgnoreCase(tag)){
                        subject = view;
                        break;
                    }
                }
            }
        }
        SubjectResponse subjectResponse;
        if (subject != null){
            subjectResponse = new SubjectResponse(subject.getName(), subject.getTag(), region.name());
        } else {
            subjectResponse = new SubjectResponse(null, null, region.name());
        }
        boolean blueTeamWin = false;
        boolean redTeamWin = false;
        int blueTeamKillCount = 0;
        int redTeamKillCount = 0;
        for (MatchDetailParticipantView view: matchDetailParticipantViewsList){
            if (view.getTeamId() == 100){
                blueTeamKillCount += view.getKills();
                if (view.getWin()){
                    blueTeamWin = true;
                }
            }
            if (view.getTeamId() == 200){
                redTeamKillCount += view.getKills();
                if (view.getWin()){
                    redTeamWin = true;
                }
            }
        }
        List<MatchDetailPlayerResponse> blueTeam = new ArrayList<>();
        List<MatchDetailPlayerResponse> redTeam = new ArrayList<>();
        for (MatchDetailParticipantView view: matchDetailParticipantViewsList){
            if (view.getTeamId() == 100){
                blueTeam.add(toPlayer(view, blueTeamKillCount, match.get().getGameDuration(), subject));
            }
            if (view.getTeamId() == 200){
                redTeam.add(toPlayer(view, redTeamKillCount, match.get().getGameDuration(), subject));
            }
        }

        List<MatchTeam> matchTeamList = matchTeamRepository.findByMatchId(match.get().getId());
        ObjectivesResponse blueTeamObjectivesResponse = null;
        ObjectivesResponse redTeamObjectivesResponse = null;
        for (MatchTeam matchTeam: matchTeamList){
            if (matchTeam.getTeamId() == 100){
                blueTeamObjectivesResponse = new ObjectivesResponse(matchTeam.getBarons(), matchTeam.getDragons(),
                        matchTeam.getHeralds(), matchTeam.getTowers(), matchTeam.getInhibitors());
            }
            if (matchTeam.getTeamId() == 200){
                redTeamObjectivesResponse = new ObjectivesResponse(matchTeam.getBarons(), matchTeam.getDragons(),
                        matchTeam.getHeralds(), matchTeam.getTowers(), matchTeam.getInhibitors());
            }
        }
        MatchDetailTeamResponse blueTeamResponse = new MatchDetailTeamResponse("blue", blueTeamWin, blueTeamObjectivesResponse, blueTeam);
        MatchDetailTeamResponse redTeamResponse = new MatchDetailTeamResponse("red", redTeamWin, redTeamObjectivesResponse, redTeam);
        Map<String, String> championKeyByPuuid = new HashMap<>();
        for (MatchDetailParticipantView view: matchDetailParticipantViewsList){
            championKeyByPuuid.put(view.getPuuid(), championCatalog.getChampionKey(view.getChampionId()));
        }
        Optional<MatchTimelinePayload> payload = matchTimelineService.getMatchTimeline(match.get().getId(), matchId, championKeyByPuuid);
        MatchBuildResponse matchBuildResponse;
        if (subject == null){
            matchBuildResponse = null;
        } else {
            //TODO: a keystonePickRate felfelé torzít. A rune_stats HAVING COUNT(*) >= 5 rúnaoldalanként
            //      szűr, így a ritka kulcsrúnák se a számlálóba, se a nevezőbe nem kerülnek be
            //      (Irelia TOP, 16.16, queue 400: 100%-ot ad a valós 94,1% helyett).
            //      Javítás: külön, kulcsrúna szintű aggregátum, szigorú küszöb nélkül.
            Double keystonePickRate = runeStatsRepository.getKeystonePickRate(region.getPlatform(), match.get().getPatch(), match.get().getQueueId(), subject.getChampionId(), subject.getTeamPosition(), subject.getKeystoneId(), MIN_GAMES);
            List<PurchaseResponse> purchases = null;
            List<String> skillOrder = null;
            if (payload.isPresent()){
                purchases = payload.get().purchases().get(subject.getPuuid());
                skillOrder = payload.get().skillOrder().get(subject.getPuuid());
            }
            matchBuildResponse = new MatchBuildResponse(purchases, skillOrder, List.of(subject.getKeystoneId(),
                    subject.getPrimaryPerk2(), subject.getPrimaryPerk3(), subject.getPrimaryPerk4()),
                    List.of(subject.getSubPerk1(), subject.getSubPerk2()),
                    List.of(subject.getStatPerkOffense(), subject.getStatPerkFlex(), subject.getStatPerkDefense()), keystonePickRate);
        }
        Instant playedAt = match.get().getGameStart().toInstant();
        int playedMinutesAgo = Math.toIntExact(Duration.between(playedAt.plusSeconds(match.get().getGameDuration()), Instant.now()).toMinutes());
        MatchDetailResponse matchDetailResponse = new MatchDetailResponse(match.get().getMatchId(), QueueNames.of(match.get().getQueueId()), match.get().getPatch(), playedAt, playedMinutesAgo, Math.toIntExact(match.get().getGameDuration()), blueTeamWin? "blue" : "red", subjectResponse, null, List.of(blueTeamResponse, redTeamResponse), payload.map(MatchTimelinePayload::timeline).orElse(null), matchBuildResponse);
        return Optional.of(matchDetailResponse);
    }

    private MatchDetailPlayerResponse toPlayer(MatchDetailParticipantView view, int teamKills, long durationSeconds, MatchDetailParticipantView subject){
        String team;
        if (view.getTeamId() == 100){
            team = "blue";
        } else {
            team = "red";
        }
        String rank = null;
        if (view.getTier() != null){
            rank = RankLabel.of(view.getTier(), view.getDivision());
        }
        int killParticipation = 0;
        if (teamKills > 0){
            killParticipation = Math.round((float) (100 * (view.getKills() + view.getAssists())) / teamKills);
        }
        double csPerMin = 0.0;
        if (durationSeconds > 0) {
            csPerMin = view.getCs() / (durationSeconds / 60.0);
        }
        List<Integer> items = new ArrayList<>();
        items.add(nullIfZero(view.getItem0()));
        items.add(nullIfZero(view.getItem1()));
        items.add(nullIfZero(view.getItem2()));
        items.add(nullIfZero(view.getItem3()));
        items.add(nullIfZero(view.getItem4()));
        items.add(nullIfZero(view.getItem5()));
        Integer trinket = nullIfZero(view.getItem6());
        return new MatchDetailPlayerResponse(championCatalog.getChampionKey(view.getChampionId()), view.getName(),
                view.getTag(), team, RoleType.fromTeamPosition(view.getTeamPosition()).getRole(), view == subject,
                rank, view.getLevel(), view.getDamageTaken(), view.getWards(), view.getKills(), view.getDeaths(),
                view.getAssists(),killParticipation, view.getDamage(), view.getCs(), view.getGold(), view.getVisionScore(),
                csPerMin, items, trinket, List.of(view.getSummoner1Id(), view.getSummoner2Id()));
    }

    private Integer nullIfZero(Integer itemId){
        if (itemId == null || itemId == 0){
            return null;
        }
        return itemId;
    }
}
