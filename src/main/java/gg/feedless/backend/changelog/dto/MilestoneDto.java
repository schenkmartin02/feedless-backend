package gg.feedless.backend.changelog.dto;

import gg.feedless.backend.changelog.Period;

public record MilestoneDto(Period period, String title, String description, String version) {
}
