package gg.feedless.backend.match;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "match_timelines")
public class MatchTimeline {
    @Id
    @Column(name = "match_id")
    private long matchId;

    @Column(name = "payload")
    @JdbcTypeCode(SqlTypes.JSON)
    private MatchTimelinePayload payload;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    //JPA-Only
    protected MatchTimeline() {}

    public MatchTimeline(long matchId, MatchTimelinePayload payload) {
        this.matchId = matchId;
        this.payload = payload;
        this.createdAt = OffsetDateTime.now();
    }

    public long getMatchId() {
        return matchId;
    }

    public MatchTimelinePayload getPayload() {
        return payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
