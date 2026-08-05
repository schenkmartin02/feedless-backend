package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface RuneStatsRepository extends JpaRepository<RuneStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO rune_stats (
            platform, patch, queue_id, champion_id, team_position, rank_tier,
            keystone_id, primary_perk_2, primary_perk_3, primary_perk_4, sub_perk_1, sub_perk_2, games, wins, updated_at
            )
            SELECT
                m.platform,
                m.patch,
                m.queue_id,
                p.champion_id,
                p.team_position,
                COALESCE(pr.tier, 'UNKNOWN'),
                p.keystone_id,
                p.primary_perk_2,
                p.primary_perk_3,
                p.primary_perk_4,
                LEAST(p.sub_perk_1, p.sub_perk_2),
                GREATEST(p.sub_perk_1, p.sub_perk_2),
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
                p.keystone_id,
                p.primary_perk_2,
                p.primary_perk_3,
                p.primary_perk_4,
                LEAST(p.sub_perk_1, p.sub_perk_2),
                GREATEST(p.sub_perk_1, p.sub_perk_2)
                HAVING COUNT(*) >= 5
            ON CONFLICT (platform, patch, queue_id, champion_id, team_position, rank_tier,
                   keystone_id, primary_perk_2, primary_perk_3, primary_perk_4, sub_perk_1, sub_perk_2)
            DO UPDATE SET
                games = EXCLUDED.games,
                wins = EXCLUDED.wins,
                updated_at = NOW()
            """, nativeQuery = true)
    int recomputeRuneStats();

    @Query(value = """
    SELECT
          keystone_id    AS "keystoneId",
          primary_perk_2 AS "primaryPerk2",
          primary_perk_3 AS "primaryPerk3",
          primary_perk_4 AS "primaryPerk4",
          sub_perk_1     AS "subPerk1",
          sub_perk_2     AS "subPerk2",
          SUM(games)     AS "games"
      FROM rune_stats
      WHERE platform      = :platform
        AND patch         = :patch
        AND queue_id      = :queueId
        AND champion_id   = :championId
        AND team_position = :teamPosition
        AND rank_tier IN (:tiers)
      GROUP BY keystone_id, primary_perk_2, primary_perk_3, primary_perk_4,
               sub_perk_1, sub_perk_2
      ORDER BY SUM(games) DESC
      LIMIT 1
    """, nativeQuery = true)
    RuneStatsView getRuneStats(@Param("platform") String platform, @Param("patch") String patch,
                               @Param("queueId") int queueId, @Param("championId") int championId,
                               @Param("teamPosition") String teamPosition, @Param("tiers") Collection<String> tiers);
}
