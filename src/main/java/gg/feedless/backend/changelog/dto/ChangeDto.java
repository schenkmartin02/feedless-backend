package gg.feedless.backend.changelog.dto;

import gg.feedless.backend.changelog.ChangeType;

public record ChangeDto(ChangeType type, String title, String description) {
}
