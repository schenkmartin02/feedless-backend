package gg.feedless.backend.changelog;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SeasonType {
    SPRING("spring"),
    SUMMER("summer"),
    AUTUMN("autumn"),
    WINTER("winter");

    private final String seasonTypeName;

    SeasonType(String seasonTypeName) {
        this.seasonTypeName = seasonTypeName;
    }

    @JsonValue
    public String getValue(){
        return this.seasonTypeName;
    }
}
