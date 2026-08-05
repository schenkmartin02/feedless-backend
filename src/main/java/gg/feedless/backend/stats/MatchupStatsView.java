package gg.feedless.backend.stats;

public interface MatchupStatsView {
    Integer getOpponentChampionId();
    Long getGames();
    Double getWinRate();
}
