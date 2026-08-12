package gg.feedless.backend.api.player;

import gg.feedless.backend.player.MatchHistoryService;
import gg.feedless.backend.player.PlayerProfileService;
import gg.feedless.backend.player.RefreshResult;
import gg.feedless.backend.stats.MatchQueueFilter;
import gg.feedless.backend.stats.RegionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class PlayerController {
    private final PlayerProfileService playerProfileService;
    private final MatchHistoryService matchHistoryService;

    private final int refreshCooldownMinutes;

    public PlayerController(PlayerProfileService playerProfileService, @Value("${crawler.refresh.cooldown-minutes}") int refreshCooldownMinutes, MatchHistoryService matchHistoryService) {
        this.playerProfileService = playerProfileService;
        this.refreshCooldownMinutes = refreshCooldownMinutes;
        this.matchHistoryService = matchHistoryService;
    }

    @GetMapping("/players/{region}/{name}/{tag}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable RegionType region, @PathVariable String name, @PathVariable String tag){
        Optional<PlayerResponse> result = playerProfileService.getPlayer(region, name, tag);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/players/{region}/{name}/{tag}/refresh")
    public ResponseEntity<Void> refreshPlayer(@PathVariable RegionType region, @PathVariable String name, @PathVariable String tag) {
        RefreshResult result = playerProfileService.requestRefresh(region, name, tag);
        return switch (result){
            case STARTED -> ResponseEntity.accepted().build();
            case NOT_FOUND -> ResponseEntity.notFound().build();
            case THROTTLED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", String.valueOf(refreshCooldownMinutes * 60)).build();
        };
    }

    @GetMapping("/players/{region}/{name}/{tag}/matches")
    public ResponseEntity<MatchHistoryResponse> getMatchHistory(@PathVariable RegionType region, @PathVariable String name, @PathVariable String tag, @RequestParam(defaultValue = "all")MatchQueueFilter queue, @RequestParam(defaultValue = "1") int page){
        if (page <= 0) {
            page = 1;
        }
        Optional<MatchHistoryResponse> result = matchHistoryService.getMatchHistory(region, name, tag, queue, page);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
