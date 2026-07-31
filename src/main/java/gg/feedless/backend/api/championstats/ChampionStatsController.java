package gg.feedless.backend.api.championstats;

import gg.feedless.backend.stats.BracketType;
import gg.feedless.backend.stats.ChampionStatsService;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RegionType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
