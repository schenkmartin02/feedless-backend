package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ChampionBanScopeRepository extends JpaRepository<ChampionBanScope, Long> {
    @Transactional
    @Modifying
    @Query(value = """
    INSERT INTO champion_ban_scope (
        platform, patch, queue_id, rank_tier, total_matches, updated_at
    )
    WITH match_tier AS (
        SELECT m.id, m.platform, m.patch, m.queue_id,
               COALESCE(mode() WITHIN GROUP (ORDER BY pr.tier), 'UNKNOWN') AS rank_tier
        FROM matches m
        JOIN participants p ON p.match_id = m.id
        LEFT JOIN player_ranks pr ON pr.player_id = p.player_id
                                 AND pr.queue_type = 'RANKED_SOLO_5x5'
        WHERE m.bans IS NOT NULL
          AND m.game_duration >= 300
          AND m.aggregated_at IS NULL
          AND m.id <= :upperBound
        GROUP BY m.id
    )
    SELECT mt.platform, mt.patch, mt.queue_id, mt.rank_tier, COUNT(*), NOW()
    FROM match_tier mt
    GROUP BY mt.platform, mt.patch, mt.queue_id, mt.rank_tier
    ON CONFLICT (platform, patch, queue_id, rank_tier)
    DO UPDATE SET
        total_matches = champion_ban_scope.total_matches + EXCLUDED.total_matches,
        updated_at    = NOW();
    """, nativeQuery = true)
    int championBanScope(@Param("upperBound") long upperBound);
}
