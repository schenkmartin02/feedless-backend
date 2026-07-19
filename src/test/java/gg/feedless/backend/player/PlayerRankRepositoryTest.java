package gg.feedless.backend.player;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class PlayerRankRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    PlayerRankRepository playerRankRepository;

    @Test
    void findByPlayerId() {
        Player testPlayer = new Player("test");
        playerRepository.save(testPlayer);
        PlayerRank testRank = new PlayerRank(testPlayer.getId(), "Ranked_Solo_5x5", "DIAMOND", "I", 70, 10, 5);
        playerRankRepository.save(testRank);

        List<PlayerRank> testRankList = playerRankRepository.findByPlayerId(testPlayer.getId());
        assertEquals("DIAMOND", testRankList.getFirst().getTier());
    }

    @Test
    void findByPlayerIdAndQueueType() {
        Player testPlayer = new Player("test");
        playerRepository.save(testPlayer);
        PlayerRank testRank = new PlayerRank(testPlayer.getId(), "Ranked_Solo_5x5", "DIAMOND", "I", 70, 10, 5);
        playerRankRepository.save(testRank);

        Optional<PlayerRank> testResult = playerRankRepository.findByPlayerIdAndQueueType(testPlayer.getId(), "Ranked_Solo_5x5");
        assertEquals(70, testResult.get().getLeaguePoints());
    }
}