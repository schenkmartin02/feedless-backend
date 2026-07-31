package gg.feedless.backend.stats;

public enum RegionType {
    EUNE("EUN1"),
    EUW("EUW1");

    private final String platform;

    RegionType(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }
}
