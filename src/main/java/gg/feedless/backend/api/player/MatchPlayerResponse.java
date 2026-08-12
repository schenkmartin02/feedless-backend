package gg.feedless.backend.api.player;

import java.util.List;

public record MatchPlayerResponse(String name, String tag, String championKey, int kills, int deaths, int assists,
                                  int cs, int damage, List<Integer> itemIds, boolean isSubject) {
}
