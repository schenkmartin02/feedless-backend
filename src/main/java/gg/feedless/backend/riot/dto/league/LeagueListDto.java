package gg.feedless.backend.riot.dto.league;

import java.util.List;

public record LeagueListDto(String leagueId, String tier, String queue, String name, List<LeagueItemDto> entries) {
}
