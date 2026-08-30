package gg.feedless.backend.api.changelog;

import gg.feedless.backend.changelog.ChangelogService;
import gg.feedless.backend.changelog.LangType;
import gg.feedless.backend.changelog.dto.ChangelogResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChangelogController {
    private final ChangelogService changelogService;

    public ChangelogController(ChangelogService changelogService) {
        this.changelogService = changelogService;
    }

    @GetMapping("/changelog")
    public ChangelogResponse getChangelog(@RequestParam LangType lang, @RequestParam(defaultValue = "false") boolean all){
        return changelogService.getChangelog(lang, all);
    }
}
