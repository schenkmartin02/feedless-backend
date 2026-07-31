package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RuneStatsRepository extends JpaRepository<RuneStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO rune_stats (
            platform, patch, queue_id, champion_id, team_position, rank_tier,
            keystone_id, games, wins, updated_at
            )
            SELECT
                m.platform,
                m.patch,
                m.queue_id,
                p.champion_id,
                p.team_position,
                COALESCE(pr.tier, 'UNKNOWN'),
                p.keystone_id,
                COUNT(*),
                COUNT(*) FILTER (WHERE p.win),
                NOW()
            FROM participants p
            JOIN matches m ON m.id = p.match_id
            LEFT JOIN player_ranks pr ON pr.player_id = p.player_id
            AND pr.queue_type = 'RANKED_SOLO_5x5'
            WHERE m.game_duration >= 300
              AND p.keystone_id > 0
            GROUP BY
                m.platform,
                m.patch,
                m.queue_id,
                p.champion_id,
                p.team_position,
                COALESCE(pr.tier, 'UNKNOWN'),
                p.keystone_id
            ON CONFLICT (platform, patch, queue_id, champion_id, team_position, rank_tier,
                   keystone_id)
            DO UPDATE SET
                games = EXCLUDED.games,
                wins = EXCLUDED.wins,
                updated_at = NOW()
            """, nativeQuery = true)
    int recomputeRuneStats();
}
