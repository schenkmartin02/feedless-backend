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
    int upsertPlayerRank(@Param("puuid") String puuid, @Param("queueType") String queueType,
                                     @Param("tier") String tier, @Param("division") String division,
                                     @Param("leaguePoints") int leaguePoints, @Param("wins") int wins, @Param("losses") int losses);
}
