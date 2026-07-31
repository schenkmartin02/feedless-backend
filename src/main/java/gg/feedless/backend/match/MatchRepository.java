package gg.feedless.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
