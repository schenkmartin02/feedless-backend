package gg.feedless.backend.stats;

public enum QueueType {
    SOLO(420),
    FLEX(440),
    ARAM(450);

    private final int queue;

    QueueType(int queue) {
        this.queue = queue;
    }

    public int getQueue() {
        return queue;
    }
}
