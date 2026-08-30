package gg.feedless.backend.changelog.dto;

import java.time.LocalDate;

public record CurrentDto(String version, LocalDate releasedAt) {
}
