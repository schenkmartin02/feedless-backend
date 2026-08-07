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
        l.delta             AS "delta"                                           \s
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

}
