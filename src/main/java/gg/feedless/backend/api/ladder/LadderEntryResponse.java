package gg.feedless.backend.api.ladder;

import java.util.List;

public record LadderEntryResponse(Integer position, String name, String tag, String region, Integer profileIconId,
                                  String tier, Integer lp, Integer wins, Integer losses, Double kda, Integer delta,
                                  List<String> topChampionKeys) {
}
