package gg.feedless.backend.changelog;

import java.util.Map;

public record RoadmapItem(StageType stage, Map<LangType, String> title, Map<LangType, String> note, Integer progress) {
}
