package gg.feedless.backend.player;

import gg.feedless.backend.api.player.MatchHistoryResponse;
import gg.feedless.backend.api.player.MatchPlayerResponse;
import gg.feedless.backend.api.player.MatchResponse;
import gg.feedless.backend.match.MatchHistoryView;
import gg.feedless.backend.match.MatchParticipantView;
import gg.feedless.backend.match.ParticipantRepository;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import gg.feedless.backend.stats.MatchQueueFilter;
import gg.feedless.backend.stats.RegionType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchHistoryService {
    private final PlayerRepository playerRepository;
    private final ParticipantRepository participantRepository;
    private final ChampionCatalog championCatalog;

    private static final int PAGE_SIZE = 20;

    public MatchHistoryService(PlayerRepository playerRepository, ParticipantRepository participantRepository, ChampionCatalog championCatalog) {
        this.playerRepository = playerRepository;
        this.participantRepository = participantRepository;
        this.championCatalog = championCatalog;
    }

    public Optional<MatchHistoryResponse> getMatchHistory(RegionType region, String gameName, String tagLine, MatchQueueFilter queue, int page){
        Optional<Player> player = playerRepository.getPlayerByNameAndTag(gameName, tagLine, region.getPlatform());
        if (player.isEmpty()) return Optional.empty();
        Long playerId = player.get().getId();
        int offset = (page - 1) * PAGE_SIZE;
        List<MatchHistoryView> matchHistoryViewsList = participantRepository.findMatchHistory(playerId, queue.getQueueId(), PAGE_SIZE + 1, offset);
        boolean hasMore = matchHistoryViewsList.size() > PAGE_SIZE;
        if (hasMore) {
            matchHistoryViewsList = matchHistoryViewsList.subList(0, PAGE_SIZE);
        }
        if (matchHistoryViewsList.isEmpty()) return Optional.of(new MatchHistoryResponse(page, false, List.of()));
        List<Long> matchIds = new ArrayList<>();
        for (MatchHistoryView view: matchHistoryViewsList){
            matchIds.add(view.getMatchRowId());
        }
        List<MatchParticipantView> participantViewList = participantRepository.findParticipantsByMatchIds(matchIds);

        Map<Long, List<MatchParticipantView>> participantsMap = participantViewList.stream().collect(Collectors.groupingBy(MatchParticipantView::getMatchRowId));

        List<MatchResponse> matchResponsesList = new ArrayList<>();
        for (MatchHistoryView view: matchHistoryViewsList){
            List<MatchParticipantView> participantViews = participantsMap.getOrDefault(view.getMatchRowId(), List.of());
            List<MatchPlayerResponse> blueTeam = new ArrayList<>();
            List<MatchPlayerResponse> redTeam = new ArrayList<>();
            for (MatchParticipantView participantView: participantViews){
                if (participantView.getTeamId() == 100){
                    blueTeam.add(toMatchPlayer(participantView, playerId));
                } else {
                    redTeam.add(toMatchPlayer(participantView, playerId));
                }
            }
            long durationSeconds = view.getDurationSeconds();
            double csPerMin = 0.0;
            double goldPerMin = 0.0;
            if (durationSeconds > 0) {
                double minutes = durationSeconds / 60.0;
                csPerMin = view.getCs() / minutes;
                goldPerMin = view.getGoldEarned() / minutes;
            }
            Instant endTime = view.getGameStart().plusSeconds(durationSeconds);
            int playedMinutesAgo = Math.toIntExact(Duration.between(endTime, Instant.now()).toMinutes());

            matchResponsesList.add(new MatchResponse(view.getMatchId(),
                    championCatalog.getChampionKey(view.getChampionId()), view.getWin(), view.getKills(),
                    view.getDeaths(), view.getAssists(), view.getCs(), csPerMin, goldPerMin, view.getLevel(),
                    queueName(view.getQueueId()), Math.toIntExact(durationSeconds), playedMinutesAgo, null,
                    badges(view, participantViews, playerId), itemIds(view.getItem0(), view.getItem1(), view.getItem2(), view.getItem3(),
                    view.getItem4(), view.getItem5(), view.getItem6()), blueTeam, redTeam));
        }
        return Optional.of(new MatchHistoryResponse(page, hasMore, matchResponsesList));
    }

    private List<Integer> itemIds(Integer item0, Integer item1, Integer item2, Integer item3, Integer item4,
                                  Integer item5, Integer item6) {
        List<Integer> result = new ArrayList<>();
        if (item0 == 0) {
            item0 = null;
        }
        if (item1 == 0) {
            item1 = null;
        }
        if (item2 == 0) {
            item2 = null;
        }
        if (item3 == 0) {
            item3 = null;
        }
        if (item4 == 0) {
            item4 = null;
        }
        if (item5 == 0) {
            item5 = null;
        }
        if (item6 == 0) {
            item6 = null;
        }
        result.add(item0);
        result.add(item1);
        result.add(item2);
        result.add(item3);
        result.add(item4);
        result.add(item5);
        result.add(item6);
        return  result;
    }

    private String queueName(int queueId){
        return switch (queueId) {
            case 420 -> "solo";
            case 440 -> "flex";
            case 450, 720 -> "aram";
            case 400, 430, 480, 490, 700 -> "normal";
            default -> "other";
        };
    }

    private MatchPlayerResponse toMatchPlayer(MatchParticipantView view, Long playerId) {
        return new MatchPlayerResponse(view.getName(), view.getTag(),
                championCatalog.getChampionKey(view.getChampionId()), view.getKills(), view.getDeaths(),
                view.getAssists(), view.getCs(), view.getDamage(), itemIds(view.getItem0(), view.getItem1(),
                view.getItem2(), view.getItem3(), view.getItem4(), view.getItem5(), view.getItem6()), Objects.equals(view.getPlayerId(), playerId));
    }

    private List<String> badges(MatchHistoryView view, List<MatchParticipantView> participantViewList, Long playerId){
        if (participantViewList.isEmpty()) return List.of();
        MatchParticipantView playerView = null;
        int maxDamage = 0;
        int maxCs = 0;
        int maxGold = 0;
        double bestWinnerKda = 0.0;
        for (MatchParticipantView participantView: participantViewList){
            if (participantView.getPlayerId().equals(playerId)) {
                playerView = participantView;
            }
            if (maxDamage < participantView.getDamage()) {
                maxDamage = participantView.getDamage();
            }
            if (maxCs < participantView.getCs()) {
                maxCs = participantView.getCs();
            }
            if (maxGold < participantView.getGoldEarned()) {
                maxGold = participantView.getGoldEarned();
            }
            if (bestWinnerKda < getKda(participantView.getKills(), participantView.getDeaths(), participantView.getAssists()) && participantView.getWin()) {
                bestWinnerKda = getKda(participantView.getKills(), participantView.getDeaths(), participantView.getAssists());
            }
        }
        if (playerView == null) {
            return List.of();
        }
        List<String> badgesList = new ArrayList<>();
        if (playerView.getWin() && bestWinnerKda == getKda(playerView.getKills(), playerView.getDeaths(), playerView.getAssists())){
            badgesList.add("MVP");
        }
        if (view.getFirstBloodKill()) {
            badgesList.add("FIRST_BLOOD");
        }
        if (playerView.getDamage() == maxDamage) {
            badgesList.add("DAMAGE_LEADER");
        }
        if (playerView.getCs() == maxCs) {
            badgesList.add("CS_LEADER");
        }
        if (playerView.getGoldEarned() == maxGold) {
            badgesList.add("GOLD_LEADER");
        }

        return badgesList;
    }

    private double getKda(int kills, int deaths, int assists){
        return (kills + assists) / (double) Math.max(1, deaths);
    }
}
