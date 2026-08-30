package gg.feedless.backend.changelog.dto;

import gg.feedless.backend.changelog.Period;

public record UpcomingDto(String version, Period eta) {
}
