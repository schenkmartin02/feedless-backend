package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChampionStatsRepository extends JpaRepository<ChampionStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO champion_stats (
                       platform, patch, queue_id, champion_id, team_position, rank_tier,
                       games, wins, sum_kills, sum_deaths, sum_assists,
                       sum_gold_earned, sum_cs, sum_damage_to_champions,
                       sum_vision_score, sum_duration, updated_at
                   )
                   SELECT
                       m.platform,
                       m.patch,
                       m.queue_id,
                       p.champion_id,
                       p.team_position,
                       COALESCE(pr.tier, 'UNKNOWN') AS rank_tier,
            
                       COUNT(*) AS games,
                       COUNT(*) FILTER (WHERE p.win) AS wins,
                       SUM(p.kills) AS sum_kills,
                       SUM(p.deaths) AS sum_deaths,
                       SUM(p.assists) AS sum_assists,
                       SUM(p.gold_earned) AS sum_gold_earned,
                       SUM(p.neutral_minions_killed + p.total_minions_killed) AS sum_cs,
                       SUM(p.total_damage_dealt_to_champions) AS sum_damage_to_champions,
                       SUM(p.vision_score) AS sum_vision_score,
                       SUM(m.game_duration) AS sum_duration,
                       NOW() AS updated_at
                   FROM participants p
                   JOIN matches m ON m.id = p.match_id
                   LEFT JOIN player_ranks pr ON pr.player_id = p.player_id
                                            AND pr.queue_type = 'RANKED_SOLO_5x5'
                   WHERE m.game_duration >= 300
                   GROUP BY
                       m.platform,
                       m.patch,
                       m.queue_id,
                       p.champion_id,
                       p.team_position,
                       COALESCE(pr.tier, 'UNKNOWN')
            
                   ON CONFLICT (platform, patch, queue_id, champion_id, team_position, rank_tier)
                   DO UPDATE SET
                       games = EXCLUDED.games,
                       wins = EXCLUDED.wins,
                       sum_kills = EXCLUDED.sum_kills,
                       sum_deaths = EXCLUDED.sum_deaths,
                       sum_assists = EXCLUDED.sum_assists,
                       sum_gold_earned = EXCLUDED.sum_gold_earned,
                       sum_cs = EXCLUDED.sum_cs,
                       sum_damage_to_champions = EXCLUDED.sum_damage_to_champions,
                       sum_vision_score = EXCLUDED.sum_vision_score,
                       sum_duration = EXCLUDED.sum_duration,
                       updated_at = NOW()
            """, nativeQuery = true)
    int recomputeChampionStats();

    @Query(value = """

            WITH aggregated AS (
        SELECT
            champion_id,
            team_position,
            SUM(games)                   AS games,
            SUM(wins)                    AS wins,
            SUM(sum_kills)               AS kills,
            SUM(sum_deaths)              AS deaths,
            SUM(sum_assists)             AS assists,
            SUM(sum_cs)                  AS cs,
            SUM(sum_gold_earned)         AS gold,
            SUM(sum_damage_to_champions) AS damage,
            SUM(sum_duration)            AS duration
        FROM champion_stats
        WHERE platform = :platform
          AND patch = :patch
          AND queue_id = :queueId
          AND rank_tier IN (:tiers)
        GROUP BY champion_id, team_position
    ),
    with_role_total AS (
        SELECT\s
            a.*,
            SUM(a.games) OVER (PARTITION BY a.team_position) AS role_total
        FROM aggregated a
    ),
    filtered AS (
        SELECT\s
            f.*,
            PERCENT_RANK() OVER (
                PARTITION BY f.team_position
                ORDER BY (f.wins + 500.0) / (f.games + 1000.0) DESC
            ) AS tier_rank,
            ROW_NUMBER() OVER (
                PARTITION BY f.team_position
                ORDER BY (f.wins + 500.0) / (f.games + 1000.0) DESC
            ) AS today_rank,
            COUNT(*) OVER (PARTITION BY f.team_position) AS role_pool
        FROM with_role_total f
        WHERE f.games >= :minGames
    )
    SELECT
        f.champion_id   AS "championId",
        f.team_position AS "teamPosition",
        f.games         AS "games",
        round((100.0 * f.wins / f.games)::numeric, 2)::float8                    AS "winRate",
        round((100.0 * f.games / f.role_total)::numeric, 2)::float8              AS "pickRate",
        round(((f.kills + f.assists)::numeric / GREATEST(f.deaths, 1)), 2)::float8 AS "kda",
        round((f.cs     * 60.0 / NULLIF(f.duration, 0))::numeric, 2)::float8     AS "csPerMinute",
        round((f.gold   * 60.0 / NULLIF(f.duration, 0))::numeric, 2)::float8     AS "goldPerMinute",
        round((f.damage * 60.0 / NULLIF(f.duration, 0))::numeric, 2)::float8     AS "damagePerMinute",
        round((f.kills::numeric   / f.games), 2)::float8                         AS "avgKills",
        round((f.deaths::numeric  / f.games), 2)::float8                         AS "avgDeaths",
        round((f.assists::numeric / f.games), 2)::float8                         AS "avgAssists",
        CASE
            WHEN f.role_pool < 20   THEN NULL::varchar
            WHEN f.tier_rank < 0.05 THEN 'S+'
            WHEN f.tier_rank < 0.15 THEN 'S'
            WHEN f.tier_rank < 0.35 THEN 'A'
            WHEN f.tier_rank < 0.65 THEN 'B'
            WHEN f.tier_rank < 0.85 THEN 'C'
            ELSE 'D'
        END AS "tier",
        (s.rank_position - f.today_rank)::int AS "trend"
    FROM filtered f
    LEFT JOIN champion_rank_snapshot s
           ON s.snapshot_date = :yesterday
          AND s.platform      = :platform
          AND s.patch         = :patch
          AND s.queue_id      = :queueId
          AND s.bracket       = :bracket
          AND s.team_position = f.team_position
          AND s.champion_id   = f.champion_id
    ORDER BY f.games DESC;
    """, nativeQuery = true)
    List<ChampionStatsView> getChampionStats(@Param("patch") String patch, @Param("queueId") int queueId,
                                             @Param("tiers")Collection<String> tiers, @Param("minGames") int minGames,
                                             @Param("platform") String platform, @Param("bracket") String bracket,
                                             @Param("yesterday") LocalDate yesterday);

    @Query(value = """
        SELECT MAX(updated_at) FROM champion_stats WHERE patch = :patch AND queue_id = :queueId AND platform = :platform
    """, nativeQuery = true)
    Optional<Instant> getLastUpdatedAt(@Param("patch") String patch, @Param("queueId") int queueId, @Param("platform") String platform);

    @Query(value = """
    SELECT COUNT(DISTINCT champion_id) FROM champion_stats
    WHERE patch = :patch AND queue_id = :queueId AND platform = :platform
    """, nativeQuery = true)
    int getTotalChampion(@Param("patch") String patch, @Param("queueId") int queueId, @Param("platform") String platform);

    @Transactional
    @Modifying
    @Query(value = """
      INSERT INTO champion_rank_snapshot (
          snapshot_date, platform, patch, queue_id, bracket,
          team_position, champion_id, rank_position
      )
      SELECT
          :snapshotDate,
          t.platform,
          t.patch,
          t.queue_id,
          :bracket,
          t.team_position,
          t.champion_id,
          ROW_NUMBER() OVER (
              PARTITION BY t.platform, t.patch, t.queue_id, t.team_position
              ORDER BY (t.wins + 500.0) / (t.games + 1000.0) DESC
          )
      FROM (
          SELECT platform, patch, queue_id, team_position, champion_id,
                 SUM(games) AS games,
                 SUM(wins)  AS wins
          FROM champion_stats
          WHERE patch = :patch
            AND rank_tier IN (:tiers)
          GROUP BY platform, patch, queue_id, team_position, champion_id
          HAVING SUM(games) >= :minGames
      ) t
      ON CONFLICT (snapshot_date, platform, patch, queue_id, bracket, team_position,
      champion_id)
      DO NOTHING
    """, nativeQuery = true)
    int insertSnapshot(@Param("snapshotDate") LocalDate snapshotDate, @Param("bracket") String bracket, @Param("patch") String patch, @Param("tiers") Collection<String> tiers, @Param("minGames") int minGames);

    @Modifying
    @Query(value = """
    DELETE FROM champion_rank_snapshot WHERE snapshot_date < :cutoff
    """, nativeQuery = true)
    int deleteOldSnapshot(@Param("cutoff") LocalDate cutoff);
}
