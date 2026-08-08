package gg.feedless.backend.api.player;

import gg.feedless.backend.player.PlayerProfileService;
import gg.feedless.backend.stats.RegionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PlayerController {
    private final PlayerProfileService playerProfileService;

    public PlayerController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @GetMapping("/players/{region}/{name}/{tag}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable RegionType region, @PathVariable String name, @PathVariable String tag){
        Optional<PlayerResponse> result = playerProfileService.getPlayer(region, name, tag);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
