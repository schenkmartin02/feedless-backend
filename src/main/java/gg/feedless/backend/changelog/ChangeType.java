package gg.feedless.backend.changelog;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeType {
    NEW("new"),
    IMPROVEMENT("improvement"),
    FIX("fix");

    private final String changeTypeName;

    ChangeType(String changeTypeName) {
        this.changeTypeName = changeTypeName;
    }

    @JsonValue
    public String getValue(){
        return this.changeTypeName;
    }
}
