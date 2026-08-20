package gg.feedless.backend.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PlayerRankRepository extends JpaRepository<PlayerRank, Long> {

    List<PlayerRank> findByPlayerId(Long playerId);

    Optional<PlayerRank> findByPlayerIdAndQueueType(Long playerId, String queueType);
    
    @Transactional
    @Modifying
    @Query(value = """

            INSERT INTO player_ranks (player_id, queue_type, tier, division,
      league_points, wins, losses)
      VALUES (
          (SELECT id FROM players WHERE puuid = :puuid),
          :queueType, :tier, :division, :leaguePoints, :wins, :losses
      )
      ON CONFLICT (player_id, queue_type) DO UPDATE
      SET tier          = EXCLUDED.tier,
          division      = EXCLUDED.division,
          league_points = EXCLUDED.league_points,
          wins          = EXCLUDED.wins,
          losses        = EXCLUDED.losses
    """, nativeQuery = true)
    void upsertPlayerRank(@Param("puuid") String puuid, @Param("queueType") String queueType, @Param("tier") String tier,
                         @Param("division") String division, @Param("leaguePoints") int leaguePoints,
                         @Param("wins") int wins, @Param("losses") int losses);

    @Query(value = """
    SELECT COALESCE((SELECT player_count FROM ranked_player_count
    WHERE platform = :platform AND queue_type = :queueType), 0)
    """, nativeQuery = true)
    long getRankedPlayerCount(@Param("queueType") String queueType, @Param("platform") String platform);

    @Transactional
    @Modifying
    @Query(value = """
    INSERT INTO ranked_player_count (platform, queue_type, player_count)
    SELECT p.platform, pr.queue_type, count(*)
    FROM player_ranks pr
    JOIN players p ON p.id = pr.player_id
    GROUP BY p.platform, pr.queue_type
    ON CONFLICT (platform, queue_type) DO UPDATE
    SET player_count = EXCLUDED.player_count,
        updated_at   = now()
    """, nativeQuery = true)
    int recomputeRankedPlayerCount();
}
