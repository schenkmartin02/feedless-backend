package gg.feedless.backend.riot.dto.league;

public record LeagueItemDto(String puuid, int leaguePoints, String rank, int wins, int losses, boolean hotStreak,
                            boolean veteran, boolean inactive, boolean freshBlood) {
}
