package gg.feedless.backend.riot.dto.timeline;

import java.util.List;
import java.util.Map;

public record FramesTimeLineDto(List<EventsTimeLineDto> events,
                                Map<String, ParticipantFrameTimeLineDto> participantFrames,
                                long timestamp) {
}
