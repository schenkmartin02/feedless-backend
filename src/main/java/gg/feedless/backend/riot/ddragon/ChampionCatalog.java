package gg.feedless.backend.riot.ddragon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChampionCatalog {
    private static final Logger log =
            LoggerFactory.getLogger(ChampionCatalog.class);
    private static final String BASE_URL =
            "https://ddragon.leagueoflegends.com";

    private final RestClient client = RestClient.create(BASE_URL);

    private Map<Integer, String> championKeys = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 86_400_000)
    public void reload() {
        log.info("Starting DDragon champion catalog reload...");
        try {
            String[] versions = client.get()
                    .uri("/api/versions.json")
                    .retrieve()
                    .body(String[].class);

            if (versions == null || versions.length == 0) {
                log.error("Version list from DDragon is empty or null");
                return;
            }

            String latestPatch = versions[0];
            log.info("Latest DDragon patch detected: {}", latestPatch);

            ChampionDDragonFull championData = client.get()
                    .uri("/cdn/{patch}/data/en_US/champion.json", latestPatch)
                    .retrieve()
                    .body(ChampionDDragonFull.class);

            if (championData == null || championData.data() == null) {
                log.error("Failed to retrieve champion data from DDragon for patch {}", latestPatch);
                return;
            }

            this.championKeys = championData.data().values().stream()
                    .collect(Collectors.toMap(
                            dto -> Integer.parseInt(dto.key()),
                            ChampionDDragon::id
                    ));
            log.info("Successfully loaded {} champions from DDragon", championKeys.size());

        } catch (Exception e) {
            log.error("Unexpected error while reloading DDragon champion catalog", e);
        }
    }

    public String getChampionKey(int championId) {
        String key = championKeys.get(championId);
        if (key == null) {
            log.warn("Unknown champion id: {}", championId);
        }
        return key;
    }
}
