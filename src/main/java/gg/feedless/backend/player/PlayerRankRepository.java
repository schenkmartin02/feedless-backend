package gg.feedless.backend.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRankRepository extends JpaRepository<PlayerRank, Long> {

    List<PlayerRank> findByPlayerId(Long playerId);

    Optional<PlayerRank> findByPlayerIdAndQueueType(Long playerId, String queueType);
}
