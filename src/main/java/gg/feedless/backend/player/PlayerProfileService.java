package gg.feedless.backend.player;

import gg.feedless.backend.api.player.PlayerResponse;
import gg.feedless.backend.api.player.RankResponse;
import gg.feedless.backend.crawl.CrawlJob;
import gg.feedless.backend.crawl.CrawlJobRepository;
import gg.feedless.backend.crawl.CrawlStatus;
import gg.feedless.backend.ladder.RankLeaderboard;
import gg.feedless.backend.ladder.RankLeaderboardRepository;
import gg.feedless.backend.match.ParticipantRepository;
import gg.feedless.backend.riot.RiotApiClient;
import gg.feedless.backend.riot.dto.account.AccountDto;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RankLabel;
import gg.feedless.backend.stats.RegionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PlayerProfileService {
    private final PlayerRankRepository playerRankRepository;
    private final PlayerRepository playerRepository;
    private final RankLeaderboardRepository rankLeaderboardRepository;
    private final ParticipantRepository participantRepository;
    private final RiotApiClient riotApiClient;
    private final CrawlJobRepository crawlJobRepository;

    private final int refreshCooldownMinutes;

    public PlayerProfileService(PlayerRankRepository playerRankRepository, PlayerRepository playerRepository, RankLeaderboardRepository rankLeaderboardRepository, ParticipantRepository participantRepository, RiotApiClient riotApiClient, CrawlJobRepository crawlJobRepository, @Value("${crawler.refresh.cooldown-minutes}") int refreshCooldownMinutes) {
        this.playerRankRepository = playerRankRepository;
        this.playerRepository = playerRepository;
        this.rankLeaderboardRepository = rankLeaderboardRepository;
        this.participantRepository = participantRepository;
        this.riotApiClient = riotApiClient;
        this.crawlJobRepository = crawlJobRepository;
        this.refreshCooldownMinutes = refreshCooldownMinutes;
    }

    public Optional<PlayerResponse> getPlayer(RegionType region, String gameName, String tagLine) {
        Optional<Player> player = playerRepository.getPlayerByNameAndTag(gameName, tagLine, region.getPlatform());
        if (player.isEmpty()) {
            Optional<AccountDto> account = riotApiClient.getAccountByNameAndTag(gameName, tagLine);
            if (account.isEmpty()){
                return Optional.empty();
            }
            AccountDto finalAccount = account.get();
            List<String> firstMatchID = riotApiClient.getFirstMatchIdByPuuid(finalAccount.puuid());
            if (firstMatchID.isEmpty()) {
                return Optional.empty();
            }
            Optional<RegionType> resolvedRegion =
                    RegionType.fromSymbol(firstMatchID.getFirst().split("_")[0]);
            if (resolvedRegion.isEmpty()) {
                return Optional.empty();
            }
            playerRepository.insertPlayer(finalAccount.puuid(), finalAccount.gameName(), finalAccount.tagLine(), resolvedRegion.get().getPlatform());
            crawlJobRepository.enqueue(finalAccount.puuid(), 2, resolvedRegion.get().getPlatform());
            return Optional.of(new PlayerResponse(finalAccount.gameName(), finalAccount.tagLine(), resolvedRegion.get().name(), 0, 0, null, null, null, null, null, List.of(), true));
        }
        Player finalPlayer = player.get();
        List<PlayerRank> playerRank = playerRankRepository.findByPlayerId(finalPlayer.getId());
        RankResponse soloRank = null;
        RankResponse flexRank = null;
        RankResponse teamRank = null;
        for (PlayerRank rank: playerRank){
            if (Objects.equals(rank.getQueueType(), "RANKED_SOLO_5x5")){
                soloRank = new RankResponse(RankLabel.of(rank.getTier(), rank.getDivision()), rank.getLeaguePoints(), rank.getWins(), rank.getLosses());
            }
            if (Objects.equals(rank.getQueueType(), "RANKED_FLEX_SR")){
                flexRank = new RankResponse(RankLabel.of(rank.getTier(), rank.getDivision()), rank.getLeaguePoints(), rank.getWins(), rank.getLosses());
            }
            if (Objects.equals(rank.getQueueType(), "RANKED_PREMADE_5x5")){
                teamRank = new RankResponse(RankLabel.of(rank.getTier(), rank.getDivision()), rank.getLeaguePoints(), rank.getWins(), rank.getLosses());
            }
        }
        Optional<RankLeaderboard> ladderRanks = rankLeaderboardRepository.findByPlatformAndQueueTypeAndPuuid(region.getPlatform(), QueueType.SOLO.getLeagueQueue(), finalPlayer.getPuuid());
        Integer ladderRank;
        if (ladderRanks.isEmpty()) {
            ladderRank = null;
        } else {
            ladderRank = ladderRanks.get().getRankPosition();
        }
        List<Boolean> form = participantRepository.getLastNMatchResult(finalPlayer.getId(), QueueType.SOLO.getQueue(), 10);
        OffsetDateTime time = finalPlayer.getProfileUpdatedAt();
        Integer updatedAtMinutesAgo = null;
        if (time != null) {
            updatedAtMinutesAgo = Math.toIntExact(Duration.between(time, OffsetDateTime.now()).toMinutes());
        }

        boolean refreshing = false;
        Optional<CrawlJob> jobResult = crawlJobRepository.findByPuuid(finalPlayer.getPuuid());
        if (jobResult.isPresent()){
            if ((jobResult.get().getStatus() == CrawlStatus.PENDING || jobResult.get().getStatus() == CrawlStatus.IN_PROGRESS) && jobResult.get().getPriority() >= 2){
                refreshing = true;
            }
        }

        return Optional.of(new PlayerResponse(finalPlayer.getGameName(), finalPlayer.getTagLine(), region.name(), finalPlayer.getProfileIconId(), finalPlayer.getSummonerLevel(), ladderRank, updatedAtMinutesAgo, soloRank, flexRank, teamRank, form, refreshing));
    }

    public RefreshResult requestRefresh(RegionType region, String gameName, String tagLine) {
        Optional<Player> player = playerRepository.getPlayerByNameAndTag(gameName, tagLine, region.getPlatform());
        if (player.isEmpty()) {
            return RefreshResult.NOT_FOUND;
        }
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(refreshCooldownMinutes);
        int result = crawlJobRepository.requestRefresh(player.get().getPuuid(), cutoff, region.getPlatform());
        if (result == 1) {
            return RefreshResult.STARTED;
        }
        return RefreshResult.THROTTLED;
    }
}
