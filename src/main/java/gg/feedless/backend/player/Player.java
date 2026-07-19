package gg.feedless.backend.player;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "puuid", nullable = false, unique = true)
    private String puuid;

    @Column(name = "game_name")
    private String gameName;

    @Column(name = "tag_line")
    private String tagLine;

    @Column(name = "profile_updated_at")
    private OffsetDateTime profileUpdatedAt;

    // JPA Only
    protected Player() {}

    public Player(String puuid) {
        this.puuid = puuid;
    }

    public Long getId() {
        return id;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public OffsetDateTime getProfileUpdatedAt() {
        return profileUpdatedAt;
    }

    public void setProfileUpdatedAt(OffsetDateTime profileUpdatedAt) {
        this.profileUpdatedAt = profileUpdatedAt;
    }
}
