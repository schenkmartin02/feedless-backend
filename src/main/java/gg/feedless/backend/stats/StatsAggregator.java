package gg.feedless.backend.stats;

import gg.feedless.backend.match.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    private final ChampionBanSnapshotRepository championBanSnapshotRepository;

    private final int batchSize;

    public StatsAggregator(ChampionStatsRepository championStatsRepository, RuneStatsRepository runeStatsRepository, ItemStatsRepository itemStatsRepository, MatchupStatsRepository matchupStatsRepository, MatchRepository matchRepository, ChampionBanStatsRepository championBanStatsRepository, ChampionBanSnapshotRepository championBanSnapshotRepository, @Value("${stats.aggregation.batch.size}") int batchSize) {
        this.championStatsRepository = championStatsRepository;
        this.runeStatsRepository = runeStatsRepository;
        this.itemStatsRepository = itemStatsRepository;
        this.matchupStatsRepository = matchupStatsRepository;
        this.matchRepository = matchRepository;
        this.championBanStatsRepository = championBanStatsRepository;
        this.championBanSnapshotRepository = championBanSnapshotRepository;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.delay.rune-ms}")
    public void recomputeRuneStats(){
        int affectedRows = runeStatsRepository.recomputeRuneStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} rune stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} rune stat rows", affectedRows);
        }
    }

    @Scheduled(fixedDelayString = "${stats.aggregation.interval-ms}", initialDelayString = "${stats.aggregation.delay.ban-ms}")
    public void recomputeChampionBanStats(){
        int affectedRows = championBanStatsRepository.recomputeChampionBanStats();
        if (affectedRows > 0) {
            log.info("Recomputed {} champion ban stat rows", affectedRows);
        } else {
            log.warn("Recomputed {} champion ban stat rows", affectedRows);
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 86_400_000, initialDelayString = "${stats.aggregation.delay.champion-snapshot-ms}")
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

    @Transactional
    @Scheduled(fixedDelay = 86_400_000, initialDelayString = "${stats.aggregation.delay.ban-snapshot-ms}")
    public void insertOrDeleteSnapshotBan() {
        Optional<String> lastPatch = matchRepository.getLastPatch();
        if (lastPatch.isEmpty()) {
            log.warn("No match to snapshot");
            return;
        }
        int result = 0;
        for (BracketType bracket: BracketType.values()) {
            result += championBanSnapshotRepository.insertNewBanSnapshot(lastPatch.get(), bracket.getTiers(), LocalDate.now(), bracket.name().toLowerCase());
        }
        int deletedResult = championBanSnapshotRepository.deleteBanSnapshot(LocalDate.now().minusDays(30));
        log.info("Snapshotted {} ban rows", result);
        log.info("Deleted {} old ban snapshot rows", deletedResult);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Scheduled(fixedDelayString = "${stats.aggregation.batch.interval-ms}")
    public void aggregateNextBatch() {
        Optional<Long> upperBound = matchRepository.getUpperBound(batchSize);
        if (upperBound.isEmpty()) {
            return;
        }
        long upperBoundGet = upperBound.get();
        int championStats = championStatsRepository.recomputeChampionStats(upperBoundGet);
        int itemStats = itemStatsRepository.recomputeItemStats(upperBoundGet);
        int matchupStats = matchupStatsRepository.recomputeMatchupStats(upperBoundGet);

        int result = matchRepository.setAggregatedAt(upperBoundGet);

        log.info("Aggregated {} matches up to id {}: {} champion, {} item, {} matchup rows", result, upperBoundGet, championStats, itemStats, matchupStats);
    }
}
