package gg.feedless.backend.stats;

import java.util.Locale;

public class RankLabel {

    private RankLabel() {}

    public static String of(String tier, String division) {
        tier = tier.charAt(0) + tier.substring(1).toLowerCase(Locale.ROOT);
        String rank = switch (division){
            case "I" -> "1";
            case "II" -> "2";
            case "III" -> "3";
            case "IV" -> "4";
            default -> null;
        };
        if (tier.equals("Master") || tier.equals("Grandmaster") || tier.equals("Challenger")) {
            rank = null;
        }
        if (rank == null) {
            return tier;
        }
        return tier + " " + rank;
    }
}
