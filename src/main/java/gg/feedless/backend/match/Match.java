package gg.feedless.backend.match;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false, unique = true)
    private String matchId;

    @Column(name = "patch", nullable = false)
    private String patch;

    @Column(name = "queue_id", nullable = false)
    private int queueId;

    @Column(name = "game_start", nullable = false)
    private OffsetDateTime gameStart;

    @Column(name = "game_duration", nullable = false)
    private long gameDuration;

    //JPA-Only
    protected Match() {}

    public Match(String matchId, String patch, int queueId, OffsetDateTime gameStart, long gameDuration) {
        this.matchId = matchId;
        this.patch = patch;
        this.queueId = queueId;
        this.gameStart = gameStart;
        this.gameDuration = gameDuration;
    }

    public Long getId() {
        return id;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getPatch() {
        return patch;
    }

    public int getQueueId() {
        return queueId;
    }

    public OffsetDateTime getGameStart() {
        return gameStart;
    }

    public long getGameDuration() {
        return gameDuration;
    }
}
