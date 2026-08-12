package gg.feedless.backend.stats;

public enum MatchQueueFilter {
    ALL(null),
    SOLO(420),
    FLEX(440),
    ARAM(450);

    private final Integer queue;

    MatchQueueFilter(Integer queue) {
        this.queue = queue;
    }

    public Integer getQueueId() {
        return queue;
    }
}
