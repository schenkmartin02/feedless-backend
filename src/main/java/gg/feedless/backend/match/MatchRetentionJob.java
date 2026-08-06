package gg.feedless.backend.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class MatchRetentionJob {
    private static final Logger log = LoggerFactory.getLogger(MatchRetentionJob.class);

    private final MatchRepository matchRepository;

    private final int retentionDays;
    private final int batchSize;

    public MatchRetentionJob(MatchRepository matchRepository, @Value("${retention.match-days}") int retentionDays, @Value("${retention.batch-size}") int batchSize){
        this.matchRepository = matchRepository;
        this.retentionDays = retentionDays;
        this.batchSize = batchSize;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${retention.interval-ms}")
    public void deleteOldMatches() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(retentionDays);
        int result = matchRepository.deleteOldMatch(cutoff, batchSize);
        if (result > 0) {
            log.info("{} old matches deleted", result);
        }
    }
}
