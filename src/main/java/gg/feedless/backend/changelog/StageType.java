package gg.feedless.backend.changelog;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StageType {
    NOW("now"),
    NEXT("next"),
    LATER("later");

    private final String stageTypeName;

    StageType(String stageTypeName) {
        this.stageTypeName = stageTypeName;
    }

    @JsonValue
    public String getValue(){
        return this.stageTypeName;
    }
}
