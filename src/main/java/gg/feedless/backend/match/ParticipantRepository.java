package gg.feedless.backend.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findAllByMatchId(Long matchId);

    Optional<Participant> findByMatchIdAndPlayerId(Long matchId, Long playerId);
}
