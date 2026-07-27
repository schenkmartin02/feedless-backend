package gg.feedless.backend.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatsAggregator {
    private static final Logger log = LoggerFactory.getLogger(StatsAggregator.class);

    private final ChampionStatsRepository championStatsRepository;

    public StatsAggregator(ChampionStatsRepository championStatsRepository) {
        this.championStatsRepository = championStatsRepository;
    }

    @Scheduled(fixedDelay = 3600_000)
    public void recomputeChampionStats(){
        int affectedRows = championStatsRepository.recomputeChampionStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} champion stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} champion stat rows", affectedRows);
        }
    }
}
