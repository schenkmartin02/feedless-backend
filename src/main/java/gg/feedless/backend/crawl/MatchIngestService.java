package gg.feedless.backend.crawl;

import gg.feedless.backend.match.Match;
import gg.feedless.backend.match.MatchRepository;
import gg.feedless.backend.match.Participant;
import gg.feedless.backend.match.ParticipantRepository;
import gg.feedless.backend.player.Player;
import gg.feedless.backend.player.PlayerRepository;
import gg.feedless.backend.riot.dto.match.MatchDto;
import gg.feedless.backend.riot.dto.match.ParticipantDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchIngestService {
    private static final Logger log = LoggerFactory.getLogger(MatchIngestService.class);

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final CrawlJobRepository crawlJobRepository;

    public MatchIngestService(PlayerRepository playerRepository, MatchRepository matchRepository, ParticipantRepository participantRepository, CrawlJobRepository crawlJobRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.participantRepository = participantRepository;
        this.crawlJobRepository = crawlJobRepository;
    }

    @Transactional
    public void ingest(MatchDto matchDto) {
        String matchId = matchDto.metadata().matchId();
        List<ParticipantDto> participantDtos = matchDto.info().participants() == null
                ? List.of()
                : matchDto.info().participants();

        for (ParticipantDto participant: participantDtos) {
            playerRepository.addNewPlayerFromMatch(participant.puuid(), participant.riotIdGameName(), participant.riotIdTagline(), participant.profileIcon());
        }
        List<String> participantPuuids = participantDtos.stream().map(ParticipantDto::puuid).toList();
        List<Player> players = playerRepository.findByPuuidIn(participantPuuids);
        Map<String, Long> playerMap = new HashMap<>();
        for (Player player: players) {
            playerMap.put(player.getPuuid(), player.getId());
        }
        OffsetDateTime gameStart = OffsetDateTime.ofInstant(Instant.ofEpochMilli(matchDto.info().gameStartTimestamp()), ZoneOffset.UTC);
        String patch = "0.0";
        String gameVersion = matchDto.info().gameVersion();
        if (gameVersion != null && !gameVersion.isBlank()) {
            String[] parts = gameVersion.split("\\.");
            if (parts.length >= 2) {
                patch = parts[0] + "." + parts[1];
            } else if (parts.length == 1) {
                patch = parts[0];
            }
        }
        Match match = new Match(matchId, patch, matchDto.info().queueId(), gameStart, matchDto.info().gameDuration());
        Match savedMatch = matchRepository.save(match);

        participantRepository.saveAll(buildParticipants(matchId, savedMatch.getId(), participantDtos, playerMap));

        for (String puuid: matchDto.metadata().participants()) {
            crawlJobRepository.enqueue(puuid, 0);
        }
    }

    private List<Participant> buildParticipants(String matchId, Long savedMatchId,
                                                List<ParticipantDto> participantDtos, Map<String, Long> playerMap) {
        if (participantDtos.isEmpty()) {
            log.warn("Match {} has no participants in info (aborted game?), storing marker row only", matchId);
            return List.of();
        }

        long distinctPuuidCount = participantDtos.stream().map(ParticipantDto::puuid).distinct().count();
        if (distinctPuuidCount != participantDtos.size()) {
            log.warn("Match {}: {} participants but only {} distinct puuids, storing the match without participants",
                    matchId, participantDtos.size(), distinctPuuidCount);
            return List.of();
        }

        List<String> unresolvedPuuids = participantDtos.stream()
                .map(ParticipantDto::puuid)
                .filter(puuid -> !playerMap.containsKey(puuid))
                .toList();
        if (!unresolvedPuuids.isEmpty()) {
            log.warn("Match {}: no player row for puuid(s) {}, storing the match without participants", matchId, unresolvedPuuids);
            return List.of();
        }

        List<Participant> participantList = new ArrayList<>();
        for (ParticipantDto participant: participantDtos) {
            participantList.add(Participant.from(savedMatchId, playerMap.get(participant.puuid()), participant));
        }
        return participantList;
    }
}
