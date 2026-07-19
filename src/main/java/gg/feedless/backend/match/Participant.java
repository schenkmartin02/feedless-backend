package gg.feedless.backend.match;

import jakarta.persistence.*;

@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "champion_id", nullable = false)
    private int championId;

    @Column(name = "team_position", nullable = false)
    private String teamPosition;

    @Column(name = "kills", nullable = false)
    private int kills;

    @Column(name = "deaths", nullable = false)
    private int deaths;

    @Column(name = "assists", nullable = false)
    private int assists;

    @Column(name = "win", nullable = false)
    private boolean win;

    //JPA-Only
    protected Participant() {}

    public Participant(Long matchId, Long playerId, int championId, String teamPosition, int kills, int deaths, int assists, boolean win) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.championId = championId;
        this.teamPosition = teamPosition;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.win = win;
    }

    public Long getId() {
        return id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public int getChampionId() {
        return championId;
    }

    public String getTeamPosition() {
        return teamPosition;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public boolean isWin() {
        return win;
    }
}
