package gg.feedless.backend.player;

import gg.feedless.backend.api.player.PlayerResponse;
import gg.feedless.backend.api.player.RankResponse;
import gg.feedless.backend.ladder.RankLeaderboard;
import gg.feedless.backend.ladder.RankLeaderboardRepository;
import gg.feedless.backend.match.ParticipantRepository;
import gg.feedless.backend.stats.QueueType;
import gg.feedless.backend.stats.RegionType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class PlayerProfileService {
    private final PlayerRankRepository playerRankRepository;
    private final PlayerRepository playerRepository;
    private final RankLeaderboardRepository rankLeaderboardRepository;
    private final ParticipantRepository participantRepository;

    public PlayerProfileService(PlayerRankRepository playerRankRepository, PlayerRepository playerRepository, RankLeaderboardRepository rankLeaderboardRepository, ParticipantRepository participantRepository) {
        this.playerRankRepository = playerRankRepository;
        this.playerRepository = playerRepository;
        this.rankLeaderboardRepository = rankLeaderboardRepository;
        this.participantRepository = participantRepository;
    }

    public Optional<PlayerResponse> getPlayer(RegionType region, String gameName, String tagLine) {
        Optional<Player> player = playerRepository.getPlayerByNameAndTag(gameName, tagLine, region.getPlatform());
        if (player.isEmpty()) {
            return Optional.empty();
        }
        Player finalPlayer = player.get();
        List<PlayerRank> playerRank = playerRankRepository.findByPlayerId(finalPlayer.getId());
        RankResponse soloRank = null;
        RankResponse flexRank = null;
        RankResponse teamRank = null;
        for (PlayerRank rank: playerRank){
            if (Objects.equals(rank.getQueueType(), "RANKED_SOLO_5x5")){
                soloRank = new RankResponse(label(rank.getTier(), rank.getDivision()), rank.getLeaguePoints(), rank.getWins(), rank.getLosses());
            }
            if (Objects.equals(rank.getQueueType(), "RANKED_FLEX_SR")){
                flexRank = new RankResponse(label(rank.getTier(), rank.getDivision()), rank.getLeaguePoints(), rank.getWins(), rank.getLosses());
            }
            if (Objects.equals(rank.getQueueType(), "RANKED_PREMADE_5x5")){
                teamRank = new RankResponse(label(rank.getTier(), rank.getDivision()), rank.getLeaguePoints(), rank.getWins(), rank.getLosses());
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


        return Optional.of(new PlayerResponse(finalPlayer.getGameName(), finalPlayer.getTagLine(), region.name(), finalPlayer.getProfileIconId(), finalPlayer.getSummonerLevel(), ladderRank, updatedAtMinutesAgo, soloRank, flexRank, teamRank, form));
    }

    private String label(String tier, String division) {
        tier = tier.charAt(0) + tier.substring(1).toLowerCase(Locale.ROOT);
        String rank = switch (division){
            case "I" -> "1";
            case "II" -> "2";
            case "III" -> "3";
            case "IV" -> "4";
            default -> null;
        };
        if (tier.equals("Master") || tier.equals("Grandmaster") || tier.equals("Challenger")) {
            rank = null;
        }
        if (rank == null) {
            return tier;
        }
        return tier + " " + rank;
    }
}
