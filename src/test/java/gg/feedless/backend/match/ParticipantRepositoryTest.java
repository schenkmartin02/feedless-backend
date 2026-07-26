package gg.feedless.backend.match;

import gg.feedless.backend.player.Player;
import gg.feedless.backend.player.PlayerRepository;
import gg.feedless.backend.riot.dto.match.ParticipantDto;
import gg.feedless.backend.riot.dto.match.PerkStatsDto;
import gg.feedless.backend.riot.dto.match.PerkStyleDto;
import gg.feedless.backend.riot.dto.match.PerkStyleSelectionDto;
import gg.feedless.backend.riot.dto.match.PerksDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class ParticipantRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    ParticipantRepository participantRepository;

    @Test
    void findAllByMatchId() {
        Player testPlayer = new Player("test");
        playerRepository.save(testPlayer);

        Player testPlayer2 = new Player("test2");
        playerRepository.save(testPlayer2);

        Match testMatch = new Match("EUN1_696969", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch);

        Participant testParticipant = Participant.from(testMatch.getId(), testPlayer.getId(),
                participantDto(69, "BOTTOM", 10, 0, 5, true));
        participantRepository.save(testParticipant);

        Participant testParticipant2 = Participant.from(testMatch.getId(), testPlayer2.getId(),
                participantDto(69, "BOTTOM", 10, 0, 5, true));
        participantRepository.save(testParticipant2);

        Match testMatch2 = new Match("EUN1_6969692", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch2);

        Participant testParticipant3 = Participant.from(testMatch2.getId(), testPlayer.getId(),
                participantDto(69, "BOTTOM", 10, 0, 5, true));
        participantRepository.save(testParticipant3);

        List<Participant> result = participantRepository.findAllByMatchId(testMatch.getId());
        List<Participant> result2 = participantRepository.findAllByMatchId(testMatch2.getId());
        assertEquals(2, result.size());
        assertEquals(1, result2.size());
    }

    @Test
    void findByMatchIdAndPlayerId() {
        Player testPlayer = new Player("test");
        playerRepository.save(testPlayer);

        Match testMatch = new Match("EUN1_696969", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch);

        Participant testParticipant = Participant.from(testMatch.getId(), testPlayer.getId(),
                participantDto(69, "BOTTOM", 10, 0, 5, true));
        participantRepository.save(testParticipant);

        Optional<Participant> result = participantRepository.findByMatchIdAndPlayerId(testMatch.getId(), testPlayer.getId());
        Optional<Participant> result2 = participantRepository.findByMatchIdAndPlayerId(10L, testPlayer.getId());
        assertTrue(result.isPresent());
        assertEquals(10, result.get().getKills());
        assertTrue(result2.isEmpty());
    }

    private static ParticipantDto participantDto(int championId, String teamPosition,
                                                 int kills, int deaths, int assists, boolean win) {
        PerksDto perks = new PerksDto(
                new PerkStatsDto(5008, 5008, 5005),
                List.of(
                        new PerkStyleDto("primaryStyle", List.of(
                                new PerkStyleSelectionDto(8112, 0, 0, 0),
                                new PerkStyleSelectionDto(8126, 0, 0, 0)), 8100),
                        new PerkStyleDto("subStyle", List.of(
                                new PerkStyleSelectionDto(8009, 0, 0, 0),
                                new PerkStyleSelectionDto(9103, 0, 0, 0)), 8000)));

        return new ParticipantDto(
                assists, 0, 18, championId, deaths, 0,
                0, false, false,
                false, 12000, 11500, teamPosition,
                0, 3153, 3006, 3031, 0, 0,
                0, 3340, kills, 5, 0, perks,
                "test-puuid", 1234, 0, "TestPlayer", "EUNE",
                4, 7,
                300, 100, 20000,
                3000, 180, 0, 25, win);
    }
}