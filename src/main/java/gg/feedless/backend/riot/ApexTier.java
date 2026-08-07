package gg.feedless.backend.riot;

public enum ApexTier {
    CHALLENGER("challengerleagues"),
    GRANDMASTER("grandmasterleagues"),
    MASTER("masterleagues");

    private final String leagues;

    ApexTier(String leagues) {
        this.leagues = leagues;
    }

    public String getLeagues() {
        return leagues;
    }
}
