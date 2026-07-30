package gg.feedless.backend.stats;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "item_stats")
public class ItemStats {
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

    @Column(name = "item_id", nullable = false)
    private int itemId;

    @Column(name = "games", nullable = false)
    private int games;

    @Column(name = "wins", nullable = false)
    private int wins;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //JPA-Only
    protected ItemStats() {}

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

    public int getItemId() {
        return itemId;
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
