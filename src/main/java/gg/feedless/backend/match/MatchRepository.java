package gg.feedless.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchId(String matchId);

    List<Match> findByMatchIdIn(Collection<String> matchIds);

    boolean existsByMatchId(String matchId);
}
