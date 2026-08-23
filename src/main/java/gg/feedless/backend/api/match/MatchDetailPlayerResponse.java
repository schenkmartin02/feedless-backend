package gg.feedless.backend.api.match;

import java.util.List;

public record MatchDetailPlayerResponse(String championKey, String name, String tag, String team, String role,
                                        boolean subject, String rank, Integer level, Integer damageTaken, Integer wards,
                                        int kills, int deaths, int assists, int killParticipation, int damage, int cs,
                                        int gold, int visionScore, double csPerMin, List<Integer> items, Integer trinket,
                                        List<Integer> summonerSpells) {
}
