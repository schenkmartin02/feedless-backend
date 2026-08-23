package gg.feedless.backend.api.match;

import gg.feedless.backend.match.MatchDetailService;
import gg.feedless.backend.stats.RegionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MatchController {
    private final MatchDetailService matchDetailService;

    public MatchController(MatchDetailService matchDetailService) {
        this.matchDetailService = matchDetailService;
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchDetailResponse> getMatchDetail(@PathVariable String matchId,
                                                              @RequestParam RegionType region,
                                                              @RequestParam(required = false) String name,
                                                              @RequestParam(required = false) String tag){
        Optional<MatchDetailResponse> result = matchDetailService.getMatchDetail(matchId, region, name, tag);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
