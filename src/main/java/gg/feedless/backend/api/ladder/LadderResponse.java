package gg.feedless.backend.api.ladder;

import java.util.List;

public record LadderResponse(String queue, String region, int page, int totalPages, Long totalPlayers,
                             Integer challengerCutoff, Long updatedMinutesAgo, List<LadderEntryResponse> entries) {
}
