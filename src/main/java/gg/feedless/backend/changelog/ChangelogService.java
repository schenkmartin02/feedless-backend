package gg.feedless.backend.changelog;

import gg.feedless.backend.changelog.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ChangelogService {
    private static final Logger log = LoggerFactory.getLogger(ChangelogService.class);
    private final ChangelogContent content;

    private static final int DEFAULT_RELEASE_COUNT = 5;

    public ChangelogService() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream in = new ClassPathResource("changelog.yml").getInputStream()) {
            this.content = mapper.readValue(in, ChangelogContent.class);
        } catch (IOException e) {
            throw new IllegalStateException("changelog.yml not readable.", e);
        }
        if (content.releases().isEmpty()) throw new IllegalStateException("changelog.yml has no releases; at least one is required");
        log.info("Loaded changelog: {} releases, {} milestones, {} roadmap items", content.releases().size(), content.milestones().size(), content.roadmap().size());
    }

    private static String resolve(Map<LangType, String> text, LangType lang){
        if (text == null) return null;
        String value = text.get(lang);
        if (value != null) return value;
        return text.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    public ChangelogResponse getChangelog(LangType lang, boolean all) {
        List<Release> source = content.releases();

        List<ReleaseDto> releases = source.stream()
                .limit(all ? source.size() : DEFAULT_RELEASE_COUNT)
                .map(r -> toDto(r, lang))
                .toList();

        List<MilestoneDto> milestones = content.milestones().stream()
                .map(m -> toDto(m, lang))
                .toList();

        List<RoadmapItemDto> roadmap = content.roadmap().stream()
                .map(i -> toDto(i, lang))
                .toList();

        Release latest = source.getFirst();
        CurrentDto current = new CurrentDto(latest.version(), latest.releasedAt());

        Upcoming up = content.upcoming();
        UpcomingDto upcoming = up == null ? null : new UpcomingDto(up.version(), up.eta());

        return new ChangelogResponse(
                content.startedAt(),
                current,
                upcoming,
                source.size(),
                releases,
                milestones,
                roadmap,
                content.nextEta(),
                content.community()
        );
    }

    private ChangeDto toDto(Change change, LangType lang){
        return new ChangeDto(change.type(), resolve(change.title(), lang), resolve(change.description(), lang));
    }

    private MilestoneDto toDto(Milestone milestone, LangType lang){
        return new MilestoneDto(milestone.period(), resolve(milestone.title(), lang), resolve(milestone.description(), lang), milestone.version());
    }

    private RoadmapItemDto toDto(RoadmapItem roadmap, LangType lang){
        return new RoadmapItemDto(roadmap.stage(), resolve(roadmap.title(), lang), resolve(roadmap.note(), lang), roadmap.progress());
    }

    private ReleaseDto toDto(Release release, LangType lang){
        return new ReleaseDto(release.version(), release.releasedAt(), resolve(release.title(), lang), release.changes().stream().map(c -> toDto(c, lang)).toList());
    }
}
