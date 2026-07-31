package gg.feedless.backend.stats;

import java.util.Set;

public enum BracketType {
    ALL("UNKNOWN", "IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "EMERALD", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"),
    EMERALD("EMERALD", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"),
    DIAMOND("DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER"),
    MASTER("MASTER", "GRANDMASTER", "CHALLENGER");

    private final Set<String> tiers;

    BracketType(String... tiers) {
        this.tiers = Set.of(tiers);
    }

    public Set<String> getTiers() {
        return tiers;
    }
}
