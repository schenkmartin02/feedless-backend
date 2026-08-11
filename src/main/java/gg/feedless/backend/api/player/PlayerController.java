package gg.feedless.backend.api.player;

import gg.feedless.backend.player.PlayerProfileService;
import gg.feedless.backend.player.RefreshResult;
import gg.feedless.backend.stats.RegionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PlayerController {
    private final PlayerProfileService playerProfileService;

    private final int refreshCooldownMinutes;

    public PlayerController(PlayerProfileService playerProfileService, @Value("${crawler.refresh.cooldown-minutes}") int refreshCooldownMinutes) {
        this.playerProfileService = playerProfileService;
        this.refreshCooldownMinutes = refreshCooldownMinutes;
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
}
