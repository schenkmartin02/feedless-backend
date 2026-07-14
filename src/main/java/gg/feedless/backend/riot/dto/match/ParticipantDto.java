package gg.feedless.backend.riot.dto.match;

public record ParticipantDto(int assists, int champLevel, int championId, String championName, int deaths,
                             boolean gameEndedInSurrender, int goldEarned, int goldSpent, String individualPosition,
                             int item0, int item1, int item2, int item3, int item4, int item5, int item6, int kills,
                             String puuid, String riotIdGameName, String riotIdTagline, int summoner1Id, int summoner2Id,
                             int summonerLevel, int teamId, int timePlayed, boolean win) {
}
