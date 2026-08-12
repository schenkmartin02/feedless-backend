package gg.feedless.backend.match;

public interface MatchParticipantView {
    Long getMatchRowId();
    Long getPlayerId();
    String getName();
    String getTag();
    Integer getChampionId();
    Integer getTeamId();
    Boolean getWin();
    Integer getKills();
    Integer getDeaths();
    Integer getAssists();
    Integer getCs();
    Integer getDamage();
    Integer getGoldEarned();
    Integer getItem0();
    Integer getItem1();
    Integer getItem2();
    Integer getItem3();
    Integer getItem4();
    Integer getItem5();
    Integer getItem6();
}
