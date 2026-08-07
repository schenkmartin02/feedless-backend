package gg.feedless.backend.api.ladder;

import gg.feedless.backend.ladder.LadderService;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RegionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LadderController {
    private final LadderService ladderService;

    public LadderController(LadderService ladderService) {
        this.ladderService = ladderService;
    }

    @GetMapping("/ladder")
    public ResponseEntity<LadderResponse> getLadder(@RequestParam QueueType queue, @RequestParam RegionType region, @RequestParam(defaultValue = "1") int page, @RequestParam(required = false) String tier, @RequestParam(required = false) String q){
        if (page < 1 || queue.getLeagueQueue() == null) {
            return ResponseEntity.badRequest().build();
        }
        LadderResponse result = ladderService.getLadder(queue, region, page, tier, q);
        return ResponseEntity.ok(result);
    }
}
