package gg.feedless.backend.changelog.dto;

import gg.feedless.backend.changelog.Community;
import gg.feedless.backend.changelog.Period;

import java.time.LocalDate;
import java.util.List;

public record ChangelogResponse(LocalDate startedAt, CurrentDto current, UpcomingDto upcoming, int totalReleases, List<ReleaseDto> releases, List<MilestoneDto> milestones, List<RoadmapItemDto> roadmap, Period nextEta, Community community) {
}
