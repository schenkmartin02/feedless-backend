package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "champion_ban_scope")
public class ChampionBanScope {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform")
    private String platform;

    @Column(name = "patch")
    private String patch;

    @Column(name = "queue_id")
    private int queueId;

    @Column(name = "rank_tier")
    private String rankTier;

    @Column(name = "total_matches")
    private Long totalMatches;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected ChampionBanScope() {}

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

    public String getRankTier() {
        return rankTier;
    }

    public Long getTotalMatches() {
        return totalMatches;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
