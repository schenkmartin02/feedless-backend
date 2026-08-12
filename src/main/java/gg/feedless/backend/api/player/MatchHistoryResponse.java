package gg.feedless.backend.api.player;

import java.util.List;

public record MatchHistoryResponse(int page, boolean hasMore, List<MatchResponse> matches) {
}
