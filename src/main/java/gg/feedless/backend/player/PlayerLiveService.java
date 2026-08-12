package gg.feedless.backend.player;

import gg.feedless.backend.api.player.LiveGameResponse;
import gg.feedless.backend.api.player.LivePlayerResponse;
import gg.feedless.backend.api.player.LiveTeamResponse;
import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import gg.feedless.backend.riot.dto.league.LeagueEntryDto;
import gg.feedless.backend.riot.dto.spectator.BannedChampionDto;
import gg.feedless.backend.riot.dto.spectator.CurrentGameDto;
import gg.feedless.backend.riot.dto.spectator.CurrentGameParticipantDto;
import gg.feedless.backend.stats.QueueNames;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RankLabel;
import gg.feedless.backend.stats.RegionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class PlayerLiveService {
    private static final Logger log = LoggerFactory.getLogger(PlayerLiveService.class);

    private final PlayerRepository playerRepository;
    private final RiotApiClient riotApiClient;
    private final ChampionCatalog championCatalog;
    private final ExecutorService executorService;

    public PlayerLiveService(PlayerRepository playerRepository, RiotApiClient riotApiClient, ChampionCatalog championCatalog, @Qualifier("rankFetchExecutor")ExecutorService executorService) {
        this.playerRepository = playerRepository;
        this.riotApiClient = riotApiClient;
        this.championCatalog = championCatalog;
        this.executorService = executorService;
    }

    public Optional<LiveGameResponse> getLiveGame(RegionType region, String gameName, String tagLine){
        Optional<Player> player = playerRepository.getPlayerByNameAndTag(gameName, tagLine, region.getPlatform());
        if (player.isEmpty()) {
            return Optional.empty();
        }
        Optional<CurrentGameDto> currentGameDto = riotApiClient.getActiveGameByPuuid(player.get().getPuuid(), region.getPlatform());
        if (currentGameDto.isEmpty()) {
            return Optional.empty();
        }
        if (currentGameDto.get().participants() == null || currentGameDto.get().participants().isEmpty()){
            return Optional.empty();
        }
        List<String> puuids = new ArrayList<>();
        for (CurrentGameParticipantDto participantDto: currentGameDto.get().participants()){
            if (participantDto.teamId() != 100 && participantDto.teamId() != 200) {
                return Optional.empty();
            }
            if (participantDto.puuid() != null){
                puuids.add(participantDto.puuid());
            }
        }
        List<Future<Set<LeagueEntryDto>>> leagueEntryFutures = new ArrayList<>();
        for(String puuid: puuids){
            leagueEntryFutures.add(executorService.submit(() -> riotApiClient.getLeagueByPuuid(puuid, region.getPlatform())));
        }
        Map<String, LeagueEntryDto> map = new HashMap<>();
        for (int i = 0; i < leagueEntryFutures.size(); i++) {
            String currentPuuid = puuids.get(i);
            try {
                Set<LeagueEntryDto> leagues = leagueEntryFutures.get(i).get();
                for(LeagueEntryDto league: leagues){
                    if (QueueType.SOLO.getLeagueQueue().equals(league.queueType())){
                        map.put(currentPuuid, league);
                    }
                }
            } catch (ExecutionException e) {
                log.error("ExecutionException for puuid: {}", currentPuuid, e);
            } catch (InterruptedException e) {
                log.error("InterruptedException while waiting for player ranks, aborting...", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
        String subjectPuuid = player.get().getPuuid();
        List<LivePlayerResponse> bluePlayers = new ArrayList<>();
        List<LivePlayerResponse> redPlayers = new ArrayList<>();
        for (CurrentGameParticipantDto participantDto: currentGameDto.get().participants()){
            String riotId = participantDto.riotId();
            int hashIndex = riotId.indexOf("#");
            String name;
            String tag = null;
            if (hashIndex >= 0) {
                name = riotId.substring(0, hashIndex);
                tag = riotId.substring(hashIndex + 1);
            } else {
                name = riotId;
            }
            LeagueEntryDto entry = null;
            if (participantDto.puuid() != null){
                entry = map.get(participantDto.puuid());
            }
            String rank = null;
            double winRate = 0.0;
            int games = 0;
            if (entry != null){
                games = entry.wins() + entry.losses();
                if (games > 0){
                    winRate = entry.wins() * 100.0 / games;
                }
                rank = RankLabel.of(entry.tier(), entry.rank());
            }
            LivePlayerResponse livePlayerResponse = new LivePlayerResponse(name, tag, championCatalog.getChampionKey(participantDto.championId()), rank, winRate, games, Objects.equals(participantDto.puuid(), subjectPuuid));
            if (participantDto.teamId() == 100){
                bluePlayers.add(livePlayerResponse);
            } else {
                redPlayers.add(livePlayerResponse);
            }
        }
        List<String> blueBans = new ArrayList<>();
        List<String> redBans = new ArrayList<>();
        if (currentGameDto.get().bannedChampions() != null){
            for (BannedChampionDto bannedChampionDto: currentGameDto.get().bannedChampions()){
                if (bannedChampionDto.championId() == -1){
                    continue;
                }
                if (bannedChampionDto.teamId() == 100){
                    blueBans.add(championCatalog.getChampionKey(bannedChampionDto.championId()));
                } else {
                    redBans.add(championCatalog.getChampionKey(bannedChampionDto.championId()));
                }
            }
        }
        int elapsedSeconds = Math.max(0, Math.toIntExact(currentGameDto.get().gameLength()));
        LiveTeamResponse blueTeam = new LiveTeamResponse("blue", blueBans, bluePlayers);
        LiveTeamResponse redTeam = new LiveTeamResponse("red", redBans, redPlayers);
        return Optional.of(new LiveGameResponse(QueueNames.of(currentGameDto.get().gameQueueConfigId()), getMapName(currentGameDto.get().mapId()), elapsedSeconds, List.of(blueTeam, redTeam)));
    }

    private String getMapName(int mapId){
        return switch (mapId){
            case 11 -> "Summoner's Rift";
            case 12 -> "Howling Abyss";
            case 21 -> "Nexus Blitz";
            case 30 -> "Arena";
            default -> "Unknown";
        };
    }
}
