package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "rune_stats")
public class RuneStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patch", nullable = false)
    private String patch;

    @Column(name = "queue_id", nullable = false)
    private int queueId;

    @Column(name = "champion_id", nullable = false)
    private int championId;

    @Column(name = "team_position", nullable = false)
    private String teamPosition;

    @Column(name = "rank_tier", nullable = false)
    private String rankTier;

    @Column(name = "keystone_id", nullable = false)
    private int keystoneId;

    @Column(name = "primary_perk_2", nullable = false)
    private int primaryPerk2;

    @Column(name = "primary_perk_3", nullable = false)
    private int primaryPerk3;

    @Column(name = "primary_perk_4", nullable = false)
    private int primaryPerk4;

    @Column(name = "sub_perk_1", nullable = false)
    private int subPerk1;

    @Column(name = "sub_perk_2", nullable = false)
    private int subPerk2;

    @Column(name = "games", nullable = false)
    private int games;

    @Column(name = "wins", nullable = false)
    private int wins;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected RuneStats() {}

    public Long getId() {
        return id;
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

    public String getTeamPosition() {
        return teamPosition;
    }

    public String getRankTier() {
        return rankTier;
    }

    public int getKeystoneId() {
        return keystoneId;
    }

    public int getPrimaryPerk2() {
        return primaryPerk2;
    }

    public int getPrimaryPerk3() {
        return primaryPerk3;
    }

    public int getPrimaryPerk4() {
        return primaryPerk4;
    }

    public int getSubPerk1() {
        return subPerk1;
    }

    public int getSubPerk2() {
        return subPerk2;
    }

    public int getGames() {
        return games;
    }

    public int getWins() {
        return wins;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
