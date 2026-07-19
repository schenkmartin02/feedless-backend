package gg.feedless.backend.match;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class MatchRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    MatchRepository matchRepository;

    @Test
    void findByMatchId() {
        Match testMatch = new Match("EUN1_696969", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch);

        Optional<Match> matchResult = matchRepository.findByMatchId("EUN1_696969");
        assertEquals(69, matchResult.get().getGameDuration());
    }

    @Test
    void findByMatchIdIn() {
        Match testMatch = new Match("EUN1_696969", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch);

        Match testMatch1 = new Match("EUN1_6969692", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch1);

        Collection<String> ids = new ArrayList<>();
        ids.add("EUN1_696969");
        ids.add("EUN1_6969692");
        ids.add("NemLetezik");
        List<Match> matchList = matchRepository.findByMatchIdIn(ids);
        assertEquals(2, matchList.size());
        assertEquals(420, matchList.getFirst().getQueueId());
    }

    @Test
    void existsByMatchId() {
        Match testMatch = new Match("EUN1_696969", "67.69.67", 420, OffsetDateTime.now(), 69);
        matchRepository.save(testMatch);

        assertTrue(matchRepository.existsByMatchId("EUN1_696969"));
        assertFalse(matchRepository.existsByMatchId("EUN1_676767"));
    }
}