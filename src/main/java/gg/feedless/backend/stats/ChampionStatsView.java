package gg.feedless.backend.stats;

public interface ChampionStatsView {
    Integer getChampionId();
    String getTeamPosition();
    Long getGames();
    Double getWinRate();
    Double getPickRate();
    Double getKda();
    Double getCsPerMinute();
    Integer getGoldPerMinute();
    Double getAvgKills();
    Double getAvgDeaths();
    Double getAvgAssists();
    String getTier();
    Integer getTrend();
    Double getBanRate();
    Integer getRoleRank();
    Integer getRolePool();
}
