package gg.feedless.backend.api.player;

import java.util.List;

public record PlayerResponse(String name, String tag, String region, int profileIconId, int level, Integer ladderRank,
                             Integer updatedMinutesAgo, RankResponse soloRank, RankResponse flexRank, RankResponse teamRank,
                             List<Boolean> form, boolean refreshing) {
}
