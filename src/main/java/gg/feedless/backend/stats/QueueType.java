package gg.feedless.backend.stats;

public enum QueueType {
    SOLO(420, "RANKED_SOLO_5x5"),
    FLEX(440, "RANKED_FLEX_SR"),
    ARAM(450, null);

    private final int queue;
    private final String leagueQueue;

    QueueType(int queue, String leagueQueue) {
        this.queue = queue;
        this.leagueQueue = leagueQueue;
    }

    public int getQueue() {
        return queue;
    }

    public String getLeagueQueue() {
        return leagueQueue;
    }
}
