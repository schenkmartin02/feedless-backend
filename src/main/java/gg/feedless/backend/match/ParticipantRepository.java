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

    @Query(value = """
    SELECT p.champion_id                                          AS "championId",
           COUNT(*)                                               AS "games",
           SUM(CASE WHEN p.win THEN 1 ELSE 0 END)                 AS "wins",
           SUM(p.kills)                                           AS "kills",
           SUM(p.deaths)                                          AS "deaths",
           SUM(p.assists)                                         AS "assists",
           SUM(p.total_minions_killed + p.neutral_minions_killed) AS "cs",
           SUM(p.gold_earned)                                     AS "gold",
           SUM(m.game_duration)                                   AS "duration"
    FROM participants p
    JOIN matches m ON m.id = p.match_id
    WHERE p.player_id = :playerId
      AND m.queue_id = :queueId
      AND m.game_duration >= 300
    GROUP BY p.champion_id
    ORDER BY COUNT(*) DESC, p.champion_id
    """, nativeQuery = true)
    List<PlayerChampionView> findPlayerChampionStats(@Param("playerId") Long playedId, @Param("queueId") int queueId);

    @Query(value = """
    SELECT pl.game_name                                      AS "name",
           pl.tag_line                                       AS "tag",
           pl.puuid                                          AS "puuid",
           p.champion_id                                     AS "championId",
           p.team_id                                         AS "teamId",
           p.team_position                                   AS "teamPosition",
           p.win                                             AS "win",
           p.kills                                           AS "kills",
           p.deaths                                          AS "deaths",
           p.assists                                         AS "assists",
           p.total_minions_killed + p.neutral_minions_killed AS "cs",
           p.total_damage_dealt_to_champions                 AS "damage",
           p.total_damage_taken                              AS "damageTaken",
           p.wards_placed                                    AS "wards",
           p.vision_score                                    AS "visionScore",
           p.gold_earned                                     AS "gold",
           p.champ_level                                     AS "level",
           p.item0 AS "item0", p.item1 AS "item1", p.item2 AS "item2",
           p.item3 AS "item3", p.item4 AS "item4", p.item5 AS "item5",
           p.item6 AS "item6",
           p.summoner1_id                                    AS "summoner1Id",
           p.summoner2_id                                    AS "summoner2Id",
           p.keystone_id                                     AS "keystoneId",
           p.primary_perk_2                                  AS "primaryPerk2",
           p.primary_perk_3                                  AS "primaryPerk3",
           p.primary_perk_4                                  AS "primaryPerk4",
           p.sub_perk_1                                      AS "subPerk1",
           p.sub_perk_2                                      AS "subPerk2",
           p.stat_perk_offense                               AS "statPerkOffense",
           p.stat_perk_flex                                  AS "statPerkFlex",
           p.stat_perk_defense                               AS "statPerkDefense",
           pr.tier                                           AS "tier",
           pr.division                                       AS "division"
    FROM participants p
    JOIN players pl ON pl.id = p.player_id
    LEFT JOIN player_ranks pr ON pr.player_id = p.player_id
                             AND pr.queue_type = 'RANKED_SOLO_5x5'
    WHERE p.match_id = :matchRowId
    ORDER BY p.team_id,
             CASE p.team_position
                 WHEN 'TOP' THEN 1
                 WHEN 'JUNGLE' THEN 2
                 WHEN 'MIDDLE' THEN 3
                 WHEN 'BOTTOM' THEN 4
                 WHEN 'UTILITY' THEN 5
                 ELSE 6
             END
    """, nativeQuery = true)
    List<MatchDetailParticipantView> findMatchDetailParticipants(@Param("matchRowId") Long matchRowId);
}
