package gg.feedless.backend.changelog;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record Release(String version, LocalDate releasedAt, Map<LangType, String> title, List<Change> changes) {
}
