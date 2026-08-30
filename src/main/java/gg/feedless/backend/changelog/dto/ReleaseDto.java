package gg.feedless.backend.changelog.dto;

import java.time.LocalDate;
import java.util.List;

public record ReleaseDto(String version, LocalDate releasedAt, String title, List<ChangeDto> changes) {
}
