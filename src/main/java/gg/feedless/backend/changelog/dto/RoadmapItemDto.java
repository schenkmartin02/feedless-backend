package gg.feedless.backend.changelog.dto;

import gg.feedless.backend.changelog.StageType;

public record RoadmapItemDto(StageType stage, String title, String note, Integer progress) {
}
