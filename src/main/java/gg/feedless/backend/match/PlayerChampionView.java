package gg.feedless.backend.match;

public interface PlayerChampionView {
    Integer getChampionId();
    Long getGames();
    Long getWins();
    Long getKills();
    Long getDeaths();
    Long getAssists();
    Long getCs();
    Long getGold();
    Long getDuration();
}
