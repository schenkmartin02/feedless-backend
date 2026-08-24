package gg.feedless.backend.match;

import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.dto.timeline.TimelineDto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class MatchTimelineService {
    private final MatchTimelineRepository matchTimelineRepository;
    private final RiotApiClient riotApiClient;

    public MatchTimelineService(MatchTimelineRepository matchTimelineRepository, RiotApiClient riotApiClient) {
        this.matchTimelineRepository = matchTimelineRepository;
        this.riotApiClient = riotApiClient;
    }

    public Optional<MatchTimelinePayload> getMatchTimeline(Long matchRowId, String matchId, Map<String, String> championKeyByPuuid) {
        Optional<MatchTimeline> matchTimeline = matchTimelineRepository.findById(matchRowId);
        if (matchTimeline.isPresent()){
            return Optional.ofNullable(matchTimeline.get().getPayload());
        }
        Optional<TimelineDto> timelineDto = riotApiClient.getMatchTimeLineByMatchId(matchId);
        if (timelineDto.isEmpty()){
            MatchTimeline newMatchTimeline = new MatchTimeline(matchRowId, null);
            matchTimelineRepository.save(newMatchTimeline);
            return Optional.empty();
        }
        MatchTimelinePayload payload = MatchTimelineMapper.toPayload(timelineDto.get(), championKeyByPuuid);
        MatchTimeline newMatchTimeline = new MatchTimeline(matchRowId, payload);
        matchTimelineRepository.save(newMatchTimeline);
        return Optional.of(payload);
    }
}
