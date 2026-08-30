package gg.feedless.backend.changelog;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LangType {
    HU("hu"),
    EN("en");

    private final String langTypeName;

    LangType(String langTypeName) {
        this.langTypeName = langTypeName;
    }

    @JsonValue
    public String getValue(){
        return this.langTypeName;
    }
}
