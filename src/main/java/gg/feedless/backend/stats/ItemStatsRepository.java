package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ItemStatsRepository extends JpaRepository<ItemStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """

            INSERT INTO item_stats (
            platform, patch, queue_id, champion_id, team_position, item_id,
            games, wins, updated_at
        )
        SELECT
            m.platform,
            m.patch,
            m.queue_id,
            p.champion_id,
            p.team_position,
            it.item_id,
            COUNT(*),
            COUNT(*) FILTER (WHERE p.win),
            NOW()
        FROM participants p
        JOIN matches m ON m.id = p.match_id
        CROSS JOIN LATERAL (VALUES (p.item0), (p.item1), (p.item2),
                                   (p.item3), (p.item4), (p.item5)) AS it(item_id)
        WHERE m.game_duration >= 300
          AND it.item_id <> 0
        GROUP BY
            m.platform,
            m.patch,
            m.queue_id,
            p.champion_id,
            p.team_position,
            it.item_id
        ON CONFLICT (platform, patch, queue_id, champion_id, team_position, item_id)
        DO UPDATE SET
            games = EXCLUDED.games,
            wins = EXCLUDED.wins,
            updated_at = NOW()
        """, nativeQuery = true)
    int recomputeItemStats();
}
