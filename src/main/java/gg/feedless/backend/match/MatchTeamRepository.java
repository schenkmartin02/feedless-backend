package gg.feedless.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {
    List<MatchTeam> findByMatchId(Long matchId);
}
