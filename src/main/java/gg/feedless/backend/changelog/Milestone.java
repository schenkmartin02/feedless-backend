package gg.feedless.backend.changelog;

import java.util.Map;

public record Milestone(Period period, Map<LangType, String> title, Map<LangType, String> description, String version) {
}
