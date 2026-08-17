package gg.feedless.backend.crawl;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "crawl_queue")
public class CrawlJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "puuid", nullable = false, unique = true)
    private String puuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CrawlStatus status;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "last_crawled_at")
    private OffsetDateTime lastCrawledAt;

    @Column(name = "retry_counter", nullable = false)
    private int retryCounter;

    @Column(name = "platform")
    private String platform;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    //JPA-Only
    protected CrawlJob() {}

    public CrawlJob(String puuid, int priority, String platform) {
        this.puuid = puuid;
        this.status = CrawlStatus.PENDING;
        this.priority = priority;
        this.createdAt = OffsetDateTime.now();
        this.platform = platform;
    }

    public Long getId() {
        return id;
    }

    public String getPuuid() {
        return puuid;
    }

    public CrawlStatus getStatus() {
        return status;
    }

    public void setStatus(CrawlStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public OffsetDateTime getLastCrawledAt() {
        return lastCrawledAt;
    }

    public void setLastCrawledAt(OffsetDateTime lastCrawledAt) {
        this.lastCrawledAt = lastCrawledAt;
    }

    public int getRetryCounter() {
        return retryCounter;
    }

    public void setRetryCounter(int retryCounter) {
        this.retryCounter = retryCounter;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
