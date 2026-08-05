package gg.feedless.backend.stats;

import gg.feedless.backend.match.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class StatsAggregator {
    private static final Logger log = LoggerFactory.getLogger(StatsAggregator.class);

    private final ChampionStatsRepository championStatsRepository;
    private final RuneStatsRepository runeStatsRepository;
    private final ItemStatsRepository itemStatsRepository;
    private final MatchupStatsRepository matchupStatsRepository;
    private final MatchRepository matchRepository;
    private final ChampionBanStatsRepository championBanStatsRepository;

    public StatsAggregator(ChampionStatsRepository championStatsRepository, RuneStatsRepository runeStatsRepository, ItemStatsRepository itemStatsRepository, MatchupStatsRepository matchupStatsRepository, MatchRepository matchRepository, ChampionBanStatsRepository championBanStatsRepository) {
        this.championStatsRepository = championStatsRepository;
        this.runeStatsRepository = runeStatsRepository;
        this.itemStatsRepository = itemStatsRepository;
        this.matchupStatsRepository = matchupStatsRepository;
        this.matchRepository = matchRepository;
        this.championBanStatsRepository = championBanStatsRepository;
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.initial-delay-ms}")
    public void recomputeChampionStats(){
        int affectedRows = championStatsRepository.recomputeChampionStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} champion stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} champion stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.initial-delay-ms}")
    public void recomputeRuneStats(){
        int affectedRows = runeStatsRepository.recomputeRuneStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} rune stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} rune stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.initial-delay-ms}")
    public void recomputeItemStats(){
        int affectedRows = itemStatsRepository.recomputeItemStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} item stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} item stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.initial-delay-ms}")
    public void recomputeMatchupStats(){
        int affectedRows = matchupStatsRepository.recomputeMatchupStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} matchup stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} matchup stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.initial-delay-ms}")
    public void recomputeChampionBanStats(){
        int affectedRows = championBanStatsRepository.recomputeChampionBanStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} champion ban stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} champion ban stat rows", affectedRows);
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 86_400_000)
    public void insertOrDeleteSnapshot() {
        Optional<String> lastPatch = matchRepository.getLastPatch();
        if (lastPatch.isEmpty()) {
            log.warn("No match to snapshot");
            return;
        }
        int result = 0;
        for (BracketType bracket: BracketType.values()) {
            result += championStatsRepository.insertSnapshot(LocalDate.now(), bracket.name().toLowerCase(), lastPatch.get(), bracket.getTiers(), ChampionStatsService.DEFAULT_MIN_GAMES);
        }
        int deletedResult = championStatsRepository.deleteOldSnapshot(LocalDate.now().minusDays(7));
        log.info("Snapshotted {} rank rows", result);
        log.info("Deleted {} old snapshot rows", deletedResult);
    }
}
