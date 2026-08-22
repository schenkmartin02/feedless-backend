package gg.feedless.backend.riot.dto.match;

public record ParticipantDto(int assists, int baronKills, int champLevel, int championId, int deaths, int doubleKills,
                             int dragonKills, boolean firstBloodKill, boolean firstTowerKill,
                             boolean gameEndedInSurrender, int goldEarned, int goldSpent, String teamPosition,
                             int inhibitorTakedowns, int item0, int item1, int item2, int item3, int item4, int item5,
                             int item6, int kills, int neutralMinionsKilled, int pentaKills, PerksDto perks,
                             String puuid, int profileIcon, int quadraKills, String riotIdGameName, String riotIdTagline,
                             int summoner1Id, int summoner2Id,
                             int summonerLevel, int teamId, int totalDamageDealtToChampions,
                             int totalHeal, int totalMinionsKilled, int tripleKills, int visionScore, boolean win,
                             int totalDamageTaken, int wardsPlaced) {
}
