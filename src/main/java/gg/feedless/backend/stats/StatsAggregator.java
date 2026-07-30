package gg.feedless.backend.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatsAggregator {
    private static final Logger log = LoggerFactory.getLogger(StatsAggregator.class);

    private final ChampionStatsRepository championStatsRepository;
    private final RuneStatsRepository runeStatsRepository;
    private final ItemStatsRepository itemStatsRepository;
    private final MatchupStatsRepository matchupStatsRepository;

    public StatsAggregator(ChampionStatsRepository championStatsRepository, RuneStatsRepository runeStatsRepository, ItemStatsRepository itemStatsRepository, MatchupStatsRepository matchupStatsRepository) {
        this.championStatsRepository = championStatsRepository;
        this.runeStatsRepository = runeStatsRepository;
        this.itemStatsRepository = itemStatsRepository;
        this.matchupStatsRepository = matchupStatsRepository;
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

    @Scheduled(fixedDelay = 3600_000)
    public void recomputeRuneStats(){
        int affectedRows = runeStatsRepository.recomputeRuneStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} rune stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} rune stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelay = 3600_000)
    public void recomputeItemStats(){
        int affectedRows = itemStatsRepository.recomputeItemStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} item stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} item stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelay = 3600_000)
    public void recomputeMatchupStats(){
        int affectedRows = matchupStatsRepository.recomputeMatchupStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} matchup stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} matchup stat rows", affectedRows);
        }
    }
}
