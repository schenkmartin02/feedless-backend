package gg.feedless.backend.stats;

public class QueueNames {
    public static String of(int queueId){
        return switch (queueId) {
            case 420 -> "solo";
            case 440 -> "flex";
            case 450, 720 -> "aram";
            case 400, 430, 480, 490, 700 -> "normal";
            default -> "other";
        };
    }

    private QueueNames() {
    }
}
