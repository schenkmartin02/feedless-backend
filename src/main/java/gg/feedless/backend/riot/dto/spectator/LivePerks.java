package gg.feedless.backend.riot.dto.spectator;

import java.util.List;

public record LivePerks(List<Long> perkIds, long perkStyle, long perkSubStyle) {
}
