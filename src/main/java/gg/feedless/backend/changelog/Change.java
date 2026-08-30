package gg.feedless.backend.changelog;

import java.util.Map;

public record Change(ChangeType type, Map<LangType, String> title, Map<LangType, String> description) {
}
