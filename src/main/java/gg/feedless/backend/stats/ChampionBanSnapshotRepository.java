package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface ChampionBanSnapshotRepository extends JpaRepository<ChampionBanSnapshot, Long> {
    @Transactional
    @Modifying
    @Query(value = """
    INSERT INTO champion_ban_snapshot (                                          \s
          snapshot_date, platform, patch, queue_id, bracket, champion_id,          \s
          bans, total_matches                                                      \s
      )                                                                            \s
      WITH scope AS (                                                              \s
          SELECT platform, patch, queue_id, rank_tier,                             \s
                 MAX(total_matches) AS total_matches                               \s
          FROM champion_ban_stats                                                  \s
          WHERE patch = :patch                                                     \s
            AND rank_tier IN (:tiers)                                              \s
          GROUP BY platform, patch, queue_id, rank_tier                            \s
      ),                                                                           \s
      scope_total AS (                                                             \s
          SELECT platform, patch, queue_id,                                        \s
                 SUM(total_matches) AS total_matches                               \s
          FROM scope                                                               \s
          GROUP BY platform, patch, queue_id                                       \s
      ),                                                                           \s
      champ AS (                                                                   \s
          SELECT platform, patch, queue_id, champion_id,                           \s
                 SUM(bans) AS bans                                                 \s
          FROM champion_ban_stats                                                  \s
          WHERE patch = :patch                                                     \s
            AND rank_tier IN (:tiers)                                              \s
          GROUP BY platform, patch, queue_id, champion_id                          \s
      )                                                                            \s
      SELECT CAST(:snapshotDate AS date),                                          \s
             c.platform,                                                           \s
             c.patch,                                                              \s
             c.queue_id,                                                           \s
             CAST(:bracket AS varchar),                                            \s
             c.champion_id,                                                        \s
             c.bans,                                                               \s
             s.total_matches                                                       \s
      FROM champ c                                                                 \s
      JOIN scope_total s ON s.platform = c.platform                                \s
                        AND s.patch    = c.patch                                   \s
                        AND s.queue_id = c.queue_id                                \s
      ON CONFLICT (snapshot_date, platform, patch, queue_id, bracket, champion_id)
      DO NOTHING
    """, nativeQuery = true)
    int insertNewBanSnapshot(@Param("patch") String patch, @Param("tiers") Collection<String> tiers,
                       @Param("snapshotDate") LocalDate snapshotDate, @Param("bracket") String bracket);

    @Transactional
    @Modifying
    @Query(value = """
    DELETE FROM champion_ban_snapshot WHERE snapshot_date < :cutoff
    """, nativeQuery = true)
    int deleteBanSnapshot(@Param("cutoff") LocalDate cutoff);

    @Transactional
    @Query(value = """
    SELECT round((100.0 * bans / NULLIF(total_matches, 0))::numeric, 2)::float8  \s
      FROM champion_ban_snapshot                                                   \s
      WHERE platform    = :platform                                                \s
        AND patch       = :patch                                                   \s
        AND queue_id    = :queueId                                                 \s
        AND bracket     = :bracket                                                 \s
        AND champion_id = :championId                                              \s
      ORDER BY snapshot_date                                                       \s
      LIMIT 1
    """, nativeQuery = true)
    Optional<Double> getPatchValue(@Param("platform") String platform, @Param("patch") String patch,
                                   @Param("queueId") int queueId, @Param("bracket") String bracket,
                                   @Param("championId") int championId);
}
