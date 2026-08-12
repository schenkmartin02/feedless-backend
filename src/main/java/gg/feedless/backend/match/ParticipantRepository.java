package gg.feedless.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findAllByMatchId(Long matchId);

    Optional<Participant> findByMatchIdAndPlayerId(Long matchId, Long playerId);

    @Query(value = """
    SELECT p.win                                                                 \s
    FROM participants p                                                          \s
    JOIN matches m ON m.id = p.match_id                                          \s
    WHERE p.player_id = :playerId                                                \s
      AND m.queue_id  = :queueId
      AND m.game_duration >= 300
    ORDER BY m.game_start DESC                                                   \s
    LIMIT :limit
    """, nativeQuery = true)
    List<Boolean> getLastNMatchResult(@Param("playerId") Long playerId, @Param("queueId") int queueId, @Param("limit") int limit);

    @Query(value = """
    SELECT m.match_id                                     AS "matchId",                                                   \s
        m.id                                              AS "matchRowId",                                                \s
        m.queue_id                                        AS "queueId",                                                   \s
        m.game_duration                                   AS "durationSeconds",                                           \s
        m.game_start                                      AS "gameStart",                                                 \s
        p.champion_id                                     AS "championId",                                                \s
        p.win                                             AS "win",                                                       \s
        p.kills                                           AS "kills",                                                     \s
        p.deaths                                          AS "deaths",                                                    \s
        p.assists                                         AS "assists",                                                   \s
        p.total_minions_killed + p.neutral_minions_killed AS "cs",                                                        \s
        p.gold_earned                                     AS "goldEarned",                                                \s
        p.champ_level                                     AS "level",                                                     \s
        p.first_blood_kill                                AS "firstBloodKill",                                            \s
        p.item0 AS "item0", p.item1 AS "item1", p.item2 AS "item2",                                                       \s
        p.item3 AS "item3", p.item4 AS "item4", p.item5 AS "item5",                                                       \s
        p.item6 AS "item6"                                                                                                \s
      FROM participants p                                                                                                      \s
      JOIN matches m ON m.id = p.match_id                                                                                      \s
      WHERE p.player_id = :playerId                                                                                            \s
        AND m.queue_id IN (400, 420, 430, 440, 450, 480, 490, 700, 720, 830, 840, 850, 900, 1900)                              \s
        AND (CAST(:queueId AS int) IS NULL OR m.queue_id = :queueId)                                                           \s
      ORDER BY m.game_start DESC                                                                                               \s
      LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<MatchHistoryView> findMatchHistory(@Param("playerId") Long playerId, @Param("queueId") Integer queueId,
                                            @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT p.match_id                                        AS "matchRowId",                                                                                                                     \s
             p.player_id                                       AS "playerId",                                                                                                                       \s
             pl.game_name                                      AS "name",                                                                                                                           \s
             pl.tag_line                                       AS "tag",                                                                                                                            \s
             p.champion_id                                     AS "championId",                                                                                                                     \s
             p.team_id                                         AS "teamId",                                                                                                                         \s
             p.win                                             AS "win",                                                                                                                            \s
             p.kills                                           AS "kills",                                                                                                                          \s
             p.deaths                                          AS "deaths",                                                                                                                         \s
             p.assists                                         AS "assists",                                                                                                                        \s
             p.total_minions_killed + p.neutral_minions_killed AS "cs",                                                                                                                             \s
             p.total_damage_dealt_to_champions                 AS "damage",                                                                                                                         \s
             p.gold_earned                                     AS "goldEarned",                                                                                                                     \s
             p.item0 AS "item0", p.item1 AS "item1", p.item2 AS "item2",                                                                                                                            \s
             p.item3 AS "item3", p.item4 AS "item4", p.item5 AS "item5",                                                                                                                            \s
             p.item6 AS "item6"                                                                                                                                                                     \s
      FROM participants p                                                                                                                                                                           \s
      JOIN players pl ON pl.id = p.player_id                                                                                                                                                        \s
      WHERE p.match_id IN (:matchRowIds)                                                                                                                                                            \s
      ORDER BY p.match_id,                                                                                                                                                                          \s
               p.team_id,                                                                                                                                                                           \s
               CASE p.team_position                                                                                                                                                                 \s
                   WHEN 'TOP' THEN 1                                                                                                                                                                \s
                   WHEN 'JUNGLE' THEN 2                                                                                                                                                             \s
                   WHEN 'MIDDLE' THEN 3                                                                                                                                                             \s
                   WHEN 'BOTTOM' THEN 4                                                                                                                                                             \s
                   WHEN 'UTILITY' THEN 5                                                                                                                                                            \s
                   ELSE 6                                                                                                                                                                           \s
               END
    """, nativeQuery = true)
    List<MatchParticipantView> findParticipantsByMatchIds(@Param("matchRowIds") List<Long> matchRowIds);
}
