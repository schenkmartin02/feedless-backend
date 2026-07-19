package gg.feedless.backend.player;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Testcontainers
class PlayerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired PlayerRepository playerRepository;



    @Test
    void findByPuuid() {
        Player testPlayer = new Player("test");
        playerRepository.save(testPlayer);

        Optional<Player> player = playerRepository.findByPuuid("test");
        assertEquals("test", player.get().getPuuid());
    }

    @Test
    void noFindPuuid() {
        Optional<Player> player = playerRepository.findByPuuid("notValidValue");
        assertEquals(Optional.empty(), player);
    }
}