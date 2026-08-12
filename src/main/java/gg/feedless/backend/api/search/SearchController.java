package gg.feedless.backend.api.search;

import gg.feedless.backend.player.PlayerSearchService;
import gg.feedless.backend.stats.RegionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {
    private final PlayerSearchService playerSearchService;

    public SearchController(PlayerSearchService playerSearchService) {
        this.playerSearchService = playerSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlayerSearchResponse>> searchPlayer(@RequestParam String q, @RequestParam RegionType region){
        List<PlayerSearchResponse> responses = playerSearchService.searchPlayer(region, q);
        return ResponseEntity.ok(responses);
    }
}
