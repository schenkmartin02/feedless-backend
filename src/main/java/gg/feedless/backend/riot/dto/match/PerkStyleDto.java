package gg.feedless.backend.riot.dto.match;

import java.util.List;

public record PerkStyleDto(String description, List<PerkStyleSelectionDto> selections, int style) {
}
