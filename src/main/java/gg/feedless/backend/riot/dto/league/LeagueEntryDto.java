package gg.feedless.backend.riot.dto.league;

public record LeagueEntryDto(String puuid, String queueType, String tier, String rank, int leaguePoints, int wins, int losses, boolean hotStreak, boolean veteran, boolean freshBlood, boolean inactive, MiniSeriesDto miniSeries) {
}
