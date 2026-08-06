package gg.feedless.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchId(String matchId);

    List<Match> findByMatchIdIn(Collection<String> matchIds);

    boolean existsByMatchId(String matchId);

    @Query(value = """
        SELECT patch FROM matches
        ORDER BY
          patch_major DESC, patch_minor DESC LIMIT 1
    """, nativeQuery = true)
    Optional<String> getLastPatch();

    @Query(value = """
    SELECT max(id) FROM (
        SELECT id FROM matches WHERE aggregated_at IS NULL ORDER BY id LIMIT
    :batchSize
    ) t
    """, nativeQuery = true)
    Optional<Long> getUpperBound(@Param("batchSize") int batchSize);

    @Modifying
    @Query(value = """
    UPDATE matches SET aggregated_at = NOW()
    WHERE aggregated_at IS NULL AND id <= :upperBound
    """, nativeQuery = true)
    int setAggregatedAt(@Param("upperBound") long upperBound);
}
