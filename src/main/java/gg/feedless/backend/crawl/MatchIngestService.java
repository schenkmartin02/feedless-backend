package gg.feedless.backend.crawl;

import gg.feedless.backend.match.Match;
import gg.feedless.backend.match.MatchRepository;
import gg.feedless.backend.match.Participant;
import gg.feedless.backend.match.ParticipantRepository;
import gg.feedless.backend.player.Player;
import gg.feedless.backend.player.PlayerRepository;
import gg.feedless.backend.riot.dto.match.MatchDto;
import gg.feedless.backend.riot.dto.match.ParticipantDto;
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
        for (ParticipantDto participant: matchDto.info().participants()) {
            playerRepository.addNewPlayerFromMatch(participant.puuid(), participant.riotIdGameName(), participant.riotIdTagline(), participant.profileIcon());
        }
        List<Player> players = playerRepository.findByPuuidIn(matchDto.metadata().participants());
        Map<String, Long> playerMap = new HashMap<>();
        for (Player player: players) {
            playerMap.put(player.getPuuid(), player.getId());
        }
        OffsetDateTime gameStart = OffsetDateTime.ofInstant(Instant.ofEpochMilli(matchDto.info().gameStartTimestamp()), ZoneOffset.UTC);
        String[] parts = matchDto.info().gameVersion().split("\\.");
        String patch = parts[0] + "." + parts[1];
        Match match = new Match(matchDto.metadata().matchId(), patch, matchDto.info().queueId(), gameStart, matchDto.info().gameDuration());
        Match savedMatch = matchRepository.save(match);
        List<Participant> participantList = new ArrayList<>();
        for (ParticipantDto participant: matchDto.info().participants()) {
            Participant newParticipant = Participant.from(savedMatch.getId(), playerMap.get(participant.puuid()), participant);
            participantList.add(newParticipant);
        }
        participantRepository.saveAll(participantList);
        for (String puuid: matchDto.metadata().participants()) {
            crawlJobRepository.enqueue(puuid, 0);
        }
    }
}
