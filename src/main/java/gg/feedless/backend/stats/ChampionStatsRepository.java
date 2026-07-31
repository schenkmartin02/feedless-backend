package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    WHERE patch = :patch
      AND queue_id = :queueId
      AND rank_tier IN (:tiers)
      AND platform = :platform
    GROUP BY champion_id, team_position), with_role_total AS (
    SELECT a.*,
           SUM(a.games) OVER (PARTITION BY a.team_position) AS role_total
    FROM aggregated a)
    SELECT
        champion_id   AS "championId",
        team_position AS "teamPosition",
        games         AS "games",
        round((100.0 * wins / games)::numeric, 2)::float8                     AS "winRate",
        round((100.0 * games / role_total)::numeric, 2)::float8               AS "pickRate",
        round(((kills + assists)::numeric / GREATEST(deaths, 1)), 2)::float8  AS "kda",
        round((cs     * 60.0 / NULLIF(duration, 0))::numeric, 2)::float8      AS "csPerMinute",
        round((gold   * 60.0 / NULLIF(duration, 0))::numeric, 2)::float8      AS "goldPerMinute",
        round((damage * 60.0 / NULLIF(duration, 0))::numeric, 2)::float8      AS "damagePerMinute",
        round((kills::numeric   / games), 2)::float8  AS "avgKills",
        round((deaths::numeric  / games), 2)::float8  AS "avgDeaths",
        round((assists::numeric / games), 2)::float8  AS "avgAssists"
    FROM with_role_total
    WHERE games >= :minGames
    ORDER BY games DESC
    """, nativeQuery = true)
    List<ChampionStatsView> getChampionStats(@Param("patch") String patch, @Param("queueId") int queueId, @Param("tiers") Collection<String> tiers, @Param("minGames") int minGames, @Param("platform") String platform);

    @Query(value = """
        SELECT MAX(updated_at) FROM champion_stats WHERE patch = :patch AND queue_id = :queueId AND platform = :platform
    """, nativeQuery = true)
    Optional<Instant> getLastUpdatedAt(@Param("patch") String patch, @Param("queueId") int queueId, @Param("platform") String platform);

    @Query(value = """
    SELECT COUNT(DISTINCT champion_id) FROM champion_stats
    WHERE patch = :patch AND queue_id = :queueId AND platform = :platform
    """, nativeQuery = true)
    int getTotalChampion(@Param("patch") String patch, @Param("queueId") int queueId, @Param("platform") String platform);
}
