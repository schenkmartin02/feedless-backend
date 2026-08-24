package gg.feedless.backend.match;

import gg.feedless.backend.api.match.PurchaseResponse;
import gg.feedless.backend.api.match.TimelineResponse;

import java.util.List;
import java.util.Map;

public record MatchTimelinePayload(TimelineResponse timeline, Map<String, List<PurchaseResponse>> purchases, Map<String, List<String>> skillOrder) {

}
