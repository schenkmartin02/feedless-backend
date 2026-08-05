package gg.feedless.backend.api.championstats;

import java.util.List;

public record BuildResponse(Double winRate, List<Integer> itemIds, List<Integer> summonerSpells, Integer spellShare) {
}
