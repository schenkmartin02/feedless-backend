package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface MatchupStatsRepository extends JpaRepository<MatchupStats, Long> {
    @Transactional
    @Modifying
    @Query(value = """

            INSERT INTO matchup_stats (
        platform, patch, queue_id, team_position, champion_id, opponent_champion_id, rank_tier,
        games, wins, updated_at
    )
    SELECT
        m.platform,
        m.patch,
        m.queue_id,
        p1.team_position,
        p1.champion_id,
        p2.champion_id,
        COALESCE(pr.tier, 'UNKNOWN'),
        COUNT(*),
        COUNT(*) FILTER (WHERE p1.win),
        NOW()
    FROM participants p1
    JOIN participants p2 ON p2.match_id      = p1.match_id
                        AND p2.team_position = p1.team_position
                        AND p2.team_id      <> p1.team_id
    JOIN matches m ON m.id = p1.match_id
    LEFT JOIN player_ranks pr ON pr.player_id = p1.player_id AND pr.queue_type = 'RANKED_SOLO_5x5'
    WHERE m.game_duration >= 300
        AND p1.team_position <> ''
        AND m.aggregated_at IS NULL
        AND m.id <= :upperBound
    GROUP BY
        m.platform,
        m.patch,
        m.queue_id,
        p1.team_position,
        p1.champion_id,
        p2.champion_id,
        COALESCE(pr.tier, 'UNKNOWN')
    ON CONFLICT (platform, patch, queue_id, champion_id, team_position, opponent_champion_id, rank_tier)
    DO UPDATE SET
        games = matchup_stats.games + EXCLUDED.games,
        wins = matchup_stats.wins + EXCLUDED.wins,
        updated_at = NOW()
    """, nativeQuery = true)
    int recomputeMatchupStats(@Param("upperBound") long upperBound);

    @Query(value = """

    SELECT
        opponent_champion_id AS "opponentChampionId",
        SUM(games)           AS "games",
        round((100.0 * SUM(wins) / SUM(games))::numeric, 2)::float8 AS "winRate"
    FROM matchup_stats
    WHERE platform      = :platform
        AND patch         = :patch
        AND queue_id      = :queueId
        AND champion_id   = :championId
        AND team_position = :teamPosition
        AND rank_tier IN (:tiers)
    GROUP BY opponent_champion_id
    HAVING SUM(games) >= :minGames
    ORDER BY (SUM(wins) + 100.0) / (SUM(games) + 200.0) DESC
    """, nativeQuery = true)
    List<MatchupStatsView> getMatchupStats(@Param("platform") String platform, @Param("patch") String patch,
                                           @Param("queueId") int queueId, @Param("championId") int championId,
                                           @Param("teamPosition") String teamPosition, @Param("tiers") Collection<String> tiers,
                                           @Param("minGames") int minGames);
}
