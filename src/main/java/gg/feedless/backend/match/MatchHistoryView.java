package gg.feedless.backend.match;

import java.time.Instant;

public interface MatchHistoryView {
    String getMatchId();
    Long getMatchRowId();
    Integer getQueueId();
    Long getDurationSeconds();
    Instant getGameStart();
    Integer getChampionId();
    Boolean getWin();
    Integer getKills();
    Integer getDeaths();
    Integer getAssists();
    Integer getCs();
    Integer getGoldEarned();
    Integer getLevel();
    Boolean getFirstBloodKill();
    Integer getItem0();
    Integer getItem1();
    Integer getItem2();
    Integer getItem3();
    Integer getItem4();
    Integer getItem5();
    Integer getItem6();
}
