package gg.feedless.backend.ladder;

import java.time.Instant;

public interface LadderProjection {
    Integer getChallengerCutoff();
    Instant getUpdatedAt();
}
