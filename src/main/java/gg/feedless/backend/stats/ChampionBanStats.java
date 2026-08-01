package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "champion_ban_stats")
public class ChampionBanStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "patch", nullable = false)
    private String patch;

    @Column(name = "queue_id", nullable = false)
    private int queueId;

    @Column(name = "champion_id", nullable = false)
    private int championId;

    @Column(name = "rank_tier", nullable = false)
    private String rankTier;

    @Column(name = "bans", nullable = false)
    private long bans;

    @Column(name = "total_matches", nullable = false)
    private long totalMatches;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected ChampionBanStats() {}

    public Long getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getPatch() {
        return patch;
    }

    public int getQueueId() {
        return queueId;
    }

    public int getChampionId() {
        return championId;
    }

    public String getRankTier() {
        return rankTier;
    }

    public long getBans() {
        return bans;
    }

    public long getTotalMatches() {
        return totalMatches;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
