package gg.feedless.backend.api.match;

import java.util.List;

public record TimelineResponse(List<Integer> goldDiff, List<Integer> killDiff, SeriesResponse series, List<TimeLineEventResponse> events, List<FirstResponse> firsts) {
}
