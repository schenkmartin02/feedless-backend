package gg.feedless.backend.changelog;

import java.time.LocalDate;
import java.util.List;

public record ChangelogContent(LocalDate startedAt, Upcoming upcoming, List<Release> releases, List<Milestone> milestones, List<RoadmapItem> roadmap, Period nextEta, Community community) {}
