package gg.feedless.backend.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SiteStatsRepository extends JpaRepository<SiteStats, Integer> {
    @Modifying
    @Query(value = """

    UPDATE site_stats
    SET analyzed_matches = (SELECT GREATEST(reltuples, 0)::bigint FROM pg_class WHERE oid = 'matches'::regclass),
    tracked_players  = (SELECT GREATEST(reltuples, 0)::bigint FROM pg_class WHERE oid = 'players'::regclass),
    champion_count   = (SELECT count(DISTINCT champion_id) FROM champion_stats),
    updated_at = now()
    WHERE id = 1
    """, nativeQuery = true)
    int recomputeSiteStats();
}
