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
}
