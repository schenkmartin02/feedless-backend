package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "site_stats")
public class SiteStats {
    @Id
    private Integer id;

    @Column(name = "analyzed_matches", nullable = false)
    private long analyzedMatches;

    @Column(name = "tracked_players", nullable = false)
    private long trackedPlayers;

    @Column(name = "champion_count", nullable = false)
    private int championCount;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected SiteStats() {}

    public int getId() {
        return id;
    }

    public long getAnalyzedMatches() {
        return analyzedMatches;
    }

    public long getTrackedPlayers() {
        return trackedPlayers;
    }

    public int getChampionCount() {
        return championCount;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
