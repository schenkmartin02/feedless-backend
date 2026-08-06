package gg.feedless.backend.api.sitestats;

import gg.feedless.backend.stats.SiteStats;
import gg.feedless.backend.stats.SiteStatsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class SiteStatsController {
    private final SiteStatsRepository siteStatsRepository;

    public SiteStatsController(SiteStatsRepository siteStatsRepository) {
        this.siteStatsRepository = siteStatsRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<SiteStatsResponse> getSiteStats(){
        Optional<SiteStats> result = siteStatsRepository.findById(1);
        if (result.isEmpty() || result.get().getAnalyzedMatches() == 0 || result.get().getTrackedPlayers() == 0 || result.get().getChampionCount() == 0) {
            return ResponseEntity.notFound().build();
        }
        SiteStats finalResult = result.get();
        return ResponseEntity.ok(new SiteStatsResponse(finalResult.getAnalyzedMatches(), finalResult.getTrackedPlayers(), finalResult.getChampionCount()));
    }
}
