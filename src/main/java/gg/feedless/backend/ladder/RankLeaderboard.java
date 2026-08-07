package gg.feedless.backend.ladder;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "rank_leaderboard")
public class RankLeaderboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform")
    private String platform;

    @Column(name = "queue_type")
    private String queueType;

    @Column(name = "rank_position")
    private int rankPosition;

    @Column(name = "puuid")
    private String puuid;

    @Column(name = "tier")
    private String tier;

    @Column(name = "league_points")
    private int leaguePoints;

    @Column(name = "wins")
    private int wins;

    @Column(name = "losses")
    private int losses;

    @Column(name = "kda")
    private Double kda;

    @Column(name = "top_champion_ids")
    private List<Integer> topChampionIds;

    @Column(name = "delta")
    private Integer delta;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected RankLeaderboard() {}

    public RankLeaderboard(String platform, String queueType, int rankPosition, String puuid, String tier, int leaguePoints, int wins, int losses, Double kda, List<Integer> topChampionIds, Integer delta) {
        this.platform = platform;
        this.queueType = queueType;
        this.rankPosition = rankPosition;
        this.puuid = puuid;
        this.tier = tier;
        this.leaguePoints = leaguePoints;
        this.wins = wins;
        this.losses = losses;
        this.kda = kda;
        this.topChampionIds = topChampionIds;
        this.delta = delta;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getQueueType() {
        return queueType;
    }

    public int getRankPosition() {
        return rankPosition;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getTier() {
        return tier;
    }

    public int getLeaguePoints() {
        return leaguePoints;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public Double getKda() {
        return kda;
    }

    public List<Integer> getTopChampionIds() {
        return topChampionIds;
    }

    public Integer getDelta() {
        return delta;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

