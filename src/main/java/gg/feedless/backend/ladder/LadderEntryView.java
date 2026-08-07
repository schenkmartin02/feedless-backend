package gg.feedless.backend.ladder;

public interface LadderEntryView {
    Integer getPosition();
    String  getName();
    String  getTag();
    Integer getProfileIconId();
    String  getTier();
    Integer getLp();
    Integer getWins();
    Integer getLosses();
    Double  getKda();
    Integer getDelta();
    Integer[] getTopChampionIds();
}
