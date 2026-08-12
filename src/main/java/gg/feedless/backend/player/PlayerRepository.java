package gg.feedless.backend.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPuuid(String puuid);

    @Modifying
    @Query(value = """
            INSERT INTO players (puuid, game_name, tag_line, profile_icon_id, platform)
            VALUES (:puuid, :gameName, :tagLine, :profileIconId, :platform)
            ON CONFLICT (puuid) DO UPDATE
            SET platform = EXCLUDED.platform
            WHERE players.platform IS DISTINCT FROM EXCLUDED.platform
            """, nativeQuery = true)
    void addNewPlayerFromMatch(
            @Param("puuid") String puuid,
            @Param("gameName") String gameName,
            @Param("tagLine") String tagLine,
            @Param("profileIconId") int profileIconId,
            @Param("platform") String platform
    );

    List<Player> findByPuuidIn(List<String> puuids);

    @Transactional
    @Query(value = """
    SELECT p.puuid
    FROM players p
    JOIN participants pa ON pa.player_id = p.id
    WHERE p.platform = :platform
    AND (p.ranks_checked_at IS NULL OR p.ranks_checked_at < :cutoff)
    AND NOT EXISTS (
        SELECT 1 FROM player_ranks pr WHERE pr.player_id = p.id
    )
    GROUP BY p.id, p.puuid
    ORDER BY count(*) DESC
    LIMIT :batchSize
    """, nativeQuery = true)
    List<String> findTopUnrankedPuuids(@Param("platform") String platform, @Param("cutoff")OffsetDateTime cutoff, @Param("batchSize") int batchSize);

    @Transactional
    @Modifying
    @Query(value = """
    INSERT INTO players (puuid, game_name, tag_line, platform)                   \s
    VALUES (:puuid, :gameName, :tagLine, :platform)                              \s
    ON CONFLICT (puuid) DO NOTHING
    """, nativeQuery = true)
    void insertPlayer(@Param("puuid") String puuid, @Param("gameName") String gameName, @Param("tagLine") String tagLine,
                      @Param("platform") String platform);

    @Transactional
    @Modifying
    @Query(value = """
    UPDATE players SET ranks_checked_at = now() WHERE puuid IN (:puuids)
    """, nativeQuery = true)
    int saveRankCheck(@Param("puuids") Collection<String> puuids);

    @Query(value = """
    SELECT p FROM Player p                                                       \s
      WHERE LOWER(p.gameName) = LOWER(:gameName)                                   \s
        AND LOWER(p.tagLine)  = LOWER(:tagLine)                                    \s
        AND p.platform        = :platform
    """)
    Optional<Player> getPlayerByNameAndTag(@Param("gameName") String gameName, @Param("tagLine") String tagLine, @Param("platform") String platform);

    @Query(value = """
    SELECT p.game_name       AS "name",                                                \s
             p.tag_line        AS "tag",                                                 \s
             p.profile_icon_id AS "profileIconId",                                       \s
             pr.tier           AS "tier",                                                \s
             pr.division       AS "division"                                             \s
      FROM players p                                                                     \s
      LEFT JOIN player_ranks pr                                                          \s
             ON pr.player_id = p.id                                                      \s
            AND pr.queue_type = 'RANKED_SOLO_5x5'                                        \s
      WHERE p.platform = :platform                                                       \s
        AND f_unaccent(lower(p.game_name)) LIKE f_unaccent(lower(:namePrefix)) || '%'    \s
        AND (CAST(:tagPrefix AS varchar) IS NULL OR lower(p.tag_line) LIKE               \s
      lower(:tagPrefix) || '%')                                                          \s
      ORDER BY f_unaccent(lower(p.game_name)) COLLATE "C"                                \s
      LIMIT :limit
    """, nativeQuery = true)
    List<PlayerSearchView> searchPlayers(@Param("platform") String platform, @Param("namePrefix") String namePrefix,
                                         @Param("tagPrefix") String tagPrefix, @Param("limit") int limit);
}
