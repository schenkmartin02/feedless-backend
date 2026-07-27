package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "champion_stats")
public class ChampionStats {
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

    @Column(name = "games", nullable = false)
    private int games;

    @Column(name = "wins", nullable = false)
    private int wins;

    @Column(name = "sum_kills", nullable = false)
    private long sumKills;

    @Column(name = "sum_deaths", nullable = false)
    private long sumDeaths;

    @Column(name = "sum_assists", nullable = false)
    private long sumAssists;

    @Column(name = "sum_gold_earned", nullable = false)
    private long sumGoldEarned;

    @Column(name = "sum_cs", nullable = false)
    private long sumCs;

    @Column(name = "sum_damage_to_champions", nullable = false)
    private long sumDamageToChampions;

    @Column(name = "sum_vision_score", nullable = false)
    private long sumVisionScore;

    @Column(name = "sum_duration", nullable = false)
    private long sumDuration;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected ChampionStats() {};

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

    public int getGames() {
        return games;
    }

    public int getWins() {
        return wins;
    }

    public long getSumKills() {
        return sumKills;
    }

    public long getSumDeaths() {
        return sumDeaths;
    }

    public long getSumAssists() {
        return sumAssists;
    }

    public long getSumGoldEarned() {
        return sumGoldEarned;
    }

    public long getSumCs() {
        return sumCs;
    }

    public long getSumDamageToChampions() {
        return sumDamageToChampions;
    }

    public long getSumVisionScore() {
        return sumVisionScore;
    }

    public long getSumDuration() {
        return sumDuration;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
