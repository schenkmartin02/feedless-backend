package gg.feedless.backend.riot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RiotApiMetrics {
    private static final Logger log = LoggerFactory.getLogger(RiotApiMetrics.class);

    private final ConcurrentHashMap<String, AtomicLong> callCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> limitMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> counts429 = new ConcurrentHashMap<>();
    private volatile Instant lastReset = Instant.now();

    public void register(String name, int per10Seconds){
        limitMap.put(name, per10Seconds);
    }

    public void recordCall(String name){
        callCounts.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordRateLimiter(String name){
        counts429.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
    }

    @Scheduled(fixedDelay = 120_000)
    public void logUsage(){
        Instant now = Instant.now();
        double elapsedSeconds = Duration.between(lastReset, now).toMillis() / 1000.0;
        lastReset = now;
        if (elapsedSeconds == 0.0){
            return;
        }
        for (Map.Entry<String, Integer> entry: limitMap.entrySet()){
            String name = entry.getKey();
            int limit = entry.getValue();

            long calls = callCounts.computeIfAbsent(name, k -> new AtomicLong()).getAndSet(0);
            long rateLimited = counts429.computeIfAbsent(name, k -> new AtomicLong()).getAndSet(0);

            double maxCalls = (limit / 10.0) * elapsedSeconds;
            double usage = calls / maxCalls * 100;

            log.info("{}: {} calls, {}%, 429: {}", name, calls, String.format("%.1f", usage), rateLimited);
        }
    }
}
