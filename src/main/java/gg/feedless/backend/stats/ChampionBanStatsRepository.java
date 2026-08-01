package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChampionBanStatsRepository extends JpaRepository<ChampionBanStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """
    INSERT INTO champion_ban_stats (
        platform, patch, queue_id, champion_id, rank_tier,
        bans, total_matches, updated_at
    )
    WITH match_tier AS (
        SELECT\s
            m.id,
            m.platform,
            m.patch,
            m.queue_id,
            m.bans,
            COALESCE(mode() WITHIN GROUP (ORDER BY pr.tier), 'UNKNOWN') AS rank_tier
        FROM matches m
        JOIN participants p ON p.match_id = m.id
        LEFT JOIN player_ranks pr ON pr.player_id = p.player_id
                                 AND pr.queue_type = 'RANKED_SOLO_5x5'
        WHERE m.bans IS NOT NULL
          AND m.game_duration >= 300
        GROUP BY m.id
    ),
    scope AS (
        SELECT\s
            platform,
            patch,
            queue_id,
            rank_tier,
            COUNT(*) AS total_matches
        FROM match_tier
        GROUP BY platform, patch, queue_id, rank_tier
    ),
    banned AS (
        SELECT DISTINCT
            mt.id,
            mt.platform,
            mt.patch,
            mt.queue_id,
            mt.rank_tier,
            ban.champion_id
        FROM match_tier mt,
             unnest(mt.bans) AS ban(champion_id)
    )
    SELECT\s
        b.platform,
        b.patch,
        b.queue_id,
        b.champion_id,
        b.rank_tier,
        COUNT(*) AS bans,
        s.total_matches,
        NOW() AS updated_at
    FROM banned b
    JOIN scope s ON s.platform  = b.platform
                AND s.patch     = b.patch
                AND s.queue_id  = b.queue_id
                AND s.rank_tier = b.rank_tier
    GROUP BY\s
        b.platform,\s
        b.patch,\s
        b.queue_id,\s
        b.champion_id,\s
        b.rank_tier,
        s.total_matches
    
    ON CONFLICT (platform, patch, queue_id, champion_id, rank_tier)
    DO UPDATE SET
        bans          = EXCLUDED.bans,
        total_matches = EXCLUDED.total_matches,
        updated_at    = NOW();
    """, nativeQuery = true)
    int recomputeChampionBanStats();
}
