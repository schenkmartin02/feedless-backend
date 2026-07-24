package gg.feedless.backend.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPuuid(String puuid);

    @Modifying
    @Query(value = """
            INSERT INTO players (puuid, game_name, tag_line, profile_icon_id)
            VALUES (:puuid, :gameName, :tagLine, :profileIconId)
            ON CONFLICT (puuid) DO NOTHING
            """, nativeQuery = true)
    void addNewPlayerFromMatch(
            @Param("puuid") String puuid,
            @Param("gameName") String gameName,
            @Param("tagLine") String tagLine,
            @Param("profileIconId") int profileIconId
    );

    List<Player> findByPuuidIn(List<String> puuids);
}
