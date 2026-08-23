package gg.feedless.backend.api.match;

import java.util.List;

public record MatchBuildResponse(List<PurchaseResponse> purchases, List<String> skillOrder, List<Integer> primaryPerkIds,
                                 List<Integer> secondaryPerkIds, List<Integer> shards, Double keystonePickRate) {
}
