package gg.feedless.backend.player;

import gg.feedless.backend.api.search.PlayerSearchResponse;
import gg.feedless.backend.stats.RankLabel;
import gg.feedless.backend.stats.RegionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerSearchService {
    private final PlayerRepository playerRepository;

    private static final int MAX_RESULTS = 5;

    public PlayerSearchService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<PlayerSearchResponse> searchPlayer(RegionType region, String q){
        String trimmed = q.trim();
        String namePrefix;
        String tagPrefix = null;
        int hashIndex = trimmed.indexOf("#");
        if (hashIndex >= 0) {
            namePrefix = trimmed.substring(0, hashIndex);
            tagPrefix = trimmed.substring(hashIndex + 1);
        } else {
            namePrefix = trimmed;
        }
        if (tagPrefix != null && tagPrefix.isBlank()){
            tagPrefix = null;
        }
        if (namePrefix.isBlank()){
            return List.of();
        }
        List<PlayerSearchView> views = playerRepository.searchPlayers(region.getPlatform(), namePrefix, tagPrefix, MAX_RESULTS);
        List<PlayerSearchResponse> responses = new ArrayList<>();
        for (PlayerSearchView view: views){
            String rank = null;
            if (view.getTier() != null){
                rank = RankLabel.of(view.getTier(), view.getDivision());
            }
            responses.add(new PlayerSearchResponse(view.getName(), view.getTag(), region.name(), view.getProfileIconId(), rank));
        }
        return responses;
    }
}
