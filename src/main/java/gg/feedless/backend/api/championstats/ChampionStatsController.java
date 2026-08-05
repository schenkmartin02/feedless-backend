package gg.feedless.backend.api.championstats;

import gg.feedless.backend.stats.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/champions")
public class ChampionStatsController {
    private final ChampionStatsService championStatsService;

    public ChampionStatsController(ChampionStatsService championStatsService) {
        this.championStatsService = championStatsService;
    }

    @GetMapping
    public ChampionStatsListResponse getChampionStats(@RequestParam QueueType queue, @RequestParam BracketType bracket, @RequestParam RegionType region) {
        return championStatsService.getChampionStats(queue, bracket, region, null, null);
    }

    @GetMapping("/featured")
    public List<FeaturedChampionResponse> getFeaturedChampionStats(@RequestParam QueueType queue, @RequestParam BracketType bracket, @RequestParam RegionType region) {
        return championStatsService.getFeaturedChampions(queue, bracket, region, null, null);
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<ChampionDetailResponse> getChampionDetail(@PathVariable String key, @RequestParam QueueType queue, @RequestParam BracketType bracket, @RequestParam RegionType region, @RequestParam(required = false) RoleType role) {
        Optional<ChampionDetailResponse> result = championStatsService.getChampionDetail(key, queue, bracket, region, role, null, null);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
