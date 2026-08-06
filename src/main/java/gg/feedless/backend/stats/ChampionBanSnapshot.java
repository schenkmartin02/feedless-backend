package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "champion_ban_snapshot")
public class ChampionBanSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "patch", nullable = false)
    private String patch;

    @Column(name = "queue_id", nullable = false)
    private int queueId;

    @Column(name = "bracket", nullable = false)
    private String bracket;

    @Column(name = "champion_id", nullable = false)
    private int championId;

    @Column(name = "bans", nullable = false)
    private long bans;

    @Column(name = "total_matches", nullable = false)
    private long totalMatches;

    //JPA-Only
    protected ChampionBanSnapshot() {}

    public Long getId() {
        return id;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
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

    public String getBracket() {
        return bracket;
    }

    public int getChampionId() {
        return championId;
    }

    public long getBans() {
        return bans;
    }

    public long getTotalMatches() {
        return totalMatches;
    }
}
