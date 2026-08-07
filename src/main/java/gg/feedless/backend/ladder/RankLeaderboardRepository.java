package gg.feedless.backend.ladder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RankLeaderboardRepository extends JpaRepository<RankLeaderboard, Long> {
    @Modifying
    @Query(value = """
    DELETE FROM rank_leaderboard
    WHERE platform = :platform AND queue_type = :queueType
    """, nativeQuery = true)
    void deleteByPlatformAndQueueType(@Param("platform") String platform, @Param("queueType") String queueType);

    @Query(value = """

    SELECT                                                                       \s
        l.rank_position     AS "position",                                       \s
        p.game_name         AS "name",                                           \s
        p.tag_line          AS "tag",                                            \s
        p.profile_icon_id   AS "profileIconId",                                  \s
        l.tier              AS "tier",                                           \s
        l.league_points     AS "lp",                                             \s
        l.wins              AS "wins",                                           \s
        l.losses            AS "losses",                                         \s
        l.kda               AS "kda",                                            \s
        l.delta             AS "delta",                                          \s
        l.top_champion_ids  AS "topChampionIds"
    FROM rank_leaderboard l                                                      \s
    LEFT JOIN players p ON p.puuid = l.puuid                                     \s
    WHERE l.platform   = :platform                                               \s
      AND l.queue_type = :queueType                                              \s
      AND l.rank_position <= :maxPosition                                        \s
      AND (CAST(:tier AS varchar) IS NULL OR l.tier = CAST(:tier AS varchar))
      AND (CAST(:q    AS varchar) IS NULL OR p.game_name ILIKE '%' || CAST(:q AS varchar) || '%')
    ORDER BY l.rank_position                                                     \s
    LIMIT :size OFFSET :offset
    """, nativeQuery = true)
    List<LadderEntryView> getRankLadder(@Param("platform") String platform, @Param("queueType") String queueType,
                                        @Param("maxPosition") int maxPosition, @Param("size") int size,
                                        @Param("offset") int offset, @Param("tier") String tier, @Param("q") String q);

    @Query(value = """
            SELECT
            (SELECT league_points FROM rank_leaderboard                              \s
            WHERE platform = :platform AND queue_type = :queueType                 \s
              AND rank_position = (SELECT count(*) FROM rank_leaderboard           \s
                                    WHERE platform = :platform AND queue_type =    \s
      :queueType                                                                   \s
                                      AND tier = 'CHALLENGER'))              AS    \s
      "challengerCutoff",                                                          \s
    
          (SELECT max(updated_at) FROM rank_leaderboard                            \s
            WHERE platform = :platform AND queue_type = :queueType)          AS    \s
      "updatedAt"
    """, nativeQuery = true)
    LadderProjection getResponseHeader(@Param("platform") String platform, @Param("queueType") String queueType);

    @Query(value = """
    SELECT count(*)                                                              \s
    FROM rank_leaderboard l                                                      \s
    LEFT JOIN players p ON p.puuid = l.puuid                                     \s
    WHERE l.platform   = :platform                                               \s
      AND l.queue_type = :queueType                                              \s
      AND l.rank_position <= :maxPosition                                        \s
      AND (CAST(:tier AS varchar) IS NULL OR l.tier = CAST(:tier AS varchar))    \s
      AND (CAST(:q    AS varchar) IS NULL OR p.game_name ILIKE '%' || CAST(:q AS \s
    varchar) || '%')
    """, nativeQuery = true)
    long countRankLadder(@Param("platform") String platform, @Param("queueType") String queueType,
                         @Param("maxPosition") int maxPosition, @Param("tier") String tier, @Param("q") String q);

    @Modifying
    @Query(value = """
    UPDATE rank_leaderboard l                                                    \s
    SET kda = s.kda                                                              \s
    FROM (                                                                       \s
        SELECT p.puuid,                                                          \s
               round(((SUM(pa.kills) + SUM(pa.assists))::numeric                 \s
                      / GREATEST(SUM(pa.deaths), 1)), 2)::float8 AS kda          \s
        FROM rank_leaderboard rl                                                 \s
        JOIN players p       ON p.puuid = rl.puuid                               \s
        JOIN participants pa ON pa.player_id = p.id                              \s
        JOIN matches m       ON m.id = pa.match_id                               \s
        WHERE rl.platform      = :platform                                       \s
          AND rl.queue_type    = :queueType                                      \s
          AND rl.rank_position <= :maxPosition                                   \s
          AND m.queue_id       = :queueId                                        \s
        GROUP BY p.puuid                                                         \s
    ) s                                                                          \s
    WHERE l.puuid      = s.puuid                                                 \s
      AND l.platform   = :platform                                               \s
      AND l.queue_type = :queueType
    """, nativeQuery = true)
    void updateRankLeaderboardKda(@Param("platform") String platform, @Param("queueType") String queueType,
                                 @Param("maxPosition") int maxPosition, @Param("queueId") int queueId);

    @Modifying
    @Query(value = """
    UPDATE rank_leaderboard l                                                    \s
    SET top_champion_ids = s.ids                                                 \s
    FROM (                                                                       \s
        SELECT puuid, array_agg(champion_id ORDER BY rn) AS ids                  \s
        FROM (                                                                   \s
            SELECT p.puuid,                                                      \s
                   pa.champion_id,                                               \s
                   ROW_NUMBER() OVER (PARTITION BY p.puuid                       \s
                                      ORDER BY count(*) DESC, pa.champion_id) AS \s
    rn                                                                           \s
            FROM rank_leaderboard rl                                             \s
            JOIN players p       ON p.puuid = rl.puuid                           \s
            JOIN participants pa ON pa.player_id = p.id                          \s
            JOIN matches m       ON m.id = pa.match_id                           \s
            WHERE rl.platform      = :platform                                   \s
              AND rl.queue_type    = :queueType                                  \s
              AND rl.rank_position <= :maxPosition                               \s
              AND m.queue_id       = :queueId                                    \s
            GROUP BY p.puuid, pa.champion_id                                     \s
        ) ranked                                                                 \s
        WHERE rn <= 3                                                            \s
        GROUP BY puuid                                                           \s
    ) s                                                                          \s
    WHERE l.puuid      = s.puuid                                                 \s
      AND l.platform   = :platform                                               \s
      AND l.queue_type = :queueType
    """, nativeQuery = true)
    void updateRankLeaderboardTopChamps(@Param("platform") String platform, @Param("queueType") String queueType,
                                       @Param("maxPosition") int maxPosition, @Param("queueId") int queueId);
}
