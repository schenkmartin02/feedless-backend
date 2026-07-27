package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ChampionStatsRepository extends JpaRepository<ChampionStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO champion_stats (
                       patch, queue_id, champion_id, team_position, rank_tier,
                       games, wins, sum_kills, sum_deaths, sum_assists,
                       sum_gold_earned, sum_cs, sum_damage_to_champions,
                       sum_vision_score, sum_duration, updated_at
                   )
                   SELECT
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
                       m.patch,
                       m.queue_id,
                       p.champion_id,
                       p.team_position,
                       COALESCE(pr.tier, 'UNKNOWN')
            
                   ON CONFLICT (patch, queue_id, champion_id, team_position, rank_tier)
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
}
