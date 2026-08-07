package gg.feedless.backend.ladder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LadderWrite {
    private final RankLeaderboardRepository rankLeaderboardRepository;

    public LadderWrite(RankLeaderboardRepository rankLeaderboardRepository) {
        this.rankLeaderboardRepository = rankLeaderboardRepository;
    }

    @Transactional
    public void replace(String platform, String queueType, List<RankLeaderboard> entries) {
        rankLeaderboardRepository.deleteByPlatformAndQueueType(platform, queueType);
        rankLeaderboardRepository.saveAll(entries);
    }
}
