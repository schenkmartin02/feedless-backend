package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MatchupStatsRepository extends JpaRepository<MatchupStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """

            INSERT INTO matchup_stats (
        platform, patch, queue_id, team_position, champion_id, opponent_champion_id,
        games, wins, updated_at
    )
    SELECT
        m.platform,
        m.patch,
        m.queue_id,
        p1.team_position,
        p1.champion_id,
        p2.champion_id,
        COUNT(*),
        COUNT(*) FILTER (WHERE p1.win),
        NOW()
    FROM participants p1
    JOIN participants p2 ON p2.match_id      = p1.match_id
                        AND p2.team_position = p1.team_position
                        AND p2.team_id      <> p1.team_id
    JOIN matches m ON m.id = p1.match_id
    WHERE m.game_duration >= 300
      AND p1.team_position <> ''
    GROUP BY
        m.platform,
        m.patch,
        m.queue_id,
        p1.team_position,
        p1.champion_id,
        p2.champion_id
    ON CONFLICT (platform, patch, queue_id, champion_id, team_position, opponent_champion_id)
    DO UPDATE SET
        games = EXCLUDED.games,
        wins = EXCLUDED.wins,
        updated_at = NOW()
    """, nativeQuery = true)
    int recomputeMatchupStats();
}
