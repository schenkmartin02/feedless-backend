package gg.feedless.backend.player;

import gg.feedless.backend.api.player.PlayerChampionResponse;
import gg.feedless.backend.match.ParticipantRepository;
import gg.feedless.backend.match.PlayerChampionView;
import gg.feedless.backend.riot.ddragon.ChampionCatalog;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RegionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerChampionService {
    private final PlayerRepository playerRepository;
    private final ParticipantRepository participantRepository;
    private final ChampionCatalog championCatalog;

    public PlayerChampionService(PlayerRepository playerRepository, ParticipantRepository participantRepository, ChampionCatalog championCatalog) {
        this.playerRepository = playerRepository;
        this.participantRepository = participantRepository;
        this.championCatalog = championCatalog;
    }

    public Optional<List<PlayerChampionResponse>> getPlayerChampions(RegionType region, String gameName, String tagLine, QueueType queue){
        Optional<Player> player = playerRepository.getPlayerByNameAndTag(gameName, tagLine, region.getPlatform());
        if (player.isEmpty()) {
            return Optional.empty();
        }
        List<PlayerChampionView> playerChampionViewList = participantRepository.findPlayerChampionStats(player.get().getId(), queue.getQueue());
        List<PlayerChampionResponse> result = new ArrayList<>();
        for (PlayerChampionView view: playerChampionViewList){
            result.add(new PlayerChampionResponse(championCatalog.getChampionKey(view.getChampionId()), Math.toIntExact(view.getGames()),
                    view.getWins() * 100.0 / view.getGames(),
                    (view.getKills() + view.getAssists()) / (double) Math.max(1, view.getDeaths()), (double) view.getKills() / view.getGames(),
                    (double) view.getDeaths() / view.getGames(), (double) view.getAssists() / view.getGames(), view.getCs() / (view.getDuration() / 60.0), view.getGold() / (view.getDuration() / 60.0)));
        }
        return Optional.of(result);
    }
}
