package gg.feedless.backend.player;

import jakarta.persistence.*;

@Entity
@Table(name = "player_ranks")
public class PlayerRank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "queue_type", nullable = false)
    private String queueType;

    @Column(name = "tier", nullable = false)
    private String tier;

    @Column(name = "division", nullable = false)
    private String division;

    @Column(name = "league_points", nullable = false)
    private int leaguePoints;

    @Column(name = "wins", nullable = false)
    private int wins;

    @Column(name = "losses", nullable = false)
    private int losses;

    //JPA-Only
    protected PlayerRank() {
    }

    public PlayerRank(Long playerId, String queueType, String tier, String division, int leaguePoints, int wins, int losses) {
        this.playerId = playerId;
        this.queueType = queueType;
        this.tier = tier;
        this.division = division;
        this.leaguePoints = leaguePoints;
        this.wins = wins;
        this.losses = losses;
    }

    public Long getId() {
        return id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getQueueType() {
        return queueType;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public int getLeaguePoints() {
        return leaguePoints;
    }

    public void setLeaguePoints(int leaguePoints) {
        this.leaguePoints = leaguePoints;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }
}
