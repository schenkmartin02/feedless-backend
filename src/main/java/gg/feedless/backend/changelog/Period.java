package gg.feedless.backend.changelog;

public record Period(int year, Integer month, Integer quarter, SeasonType season) {
    public Period {
        int notNullNumber = 0;
        if (month != null){
            notNullNumber++;
        }
        if (quarter != null){
            notNullNumber++;
        }
        if (season != null){
            notNullNumber++;
        }
        if (notNullNumber > 1){
            throw new IllegalArgumentException("Max not null in Period is 1 but now: " + notNullNumber);
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Period " + year + ": Month number is outside normal form 1-12 value is: " + month);
        }
        if (quarter != null && (quarter < 1 || quarter > 4)) {
            throw new IllegalArgumentException("Period " + year + ": Quarter number is outside normal form 1-4 value is: " + quarter);
        }
    }
}
