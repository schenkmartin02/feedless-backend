package gg.feedless.backend.match;

import gg.feedless.backend.riot.dto.match.ParticipantDto;
import gg.feedless.backend.riot.dto.match.PerksDto;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "participants")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "champion_id", nullable = false)
    private int championId;

    @Column(name = "team_position", nullable = false)
    private String teamPosition;

    @Column(name = "kills", nullable = false)
    private int kills;

    @Column(name = "deaths", nullable = false)
    private int deaths;

    @Column(name = "assists", nullable = false)
    private int assists;

    @Column(name = "win", nullable = false)
    private boolean win;

    // --- ÚJ MEZŐK ---
    @Column(name = "baron_kills", nullable = false)
    private int baronKills;

    @Column(name = "champ_level", nullable = false)
    private int champLevel;

    @Column(name = "double_kills", nullable = false)
    private int doubleKills;

    @Column(name = "dragon_kills", nullable = false)
    private int dragonKills;

    @Column(name = "first_blood_kill", nullable = false)
    private boolean firstBloodKill;

    @Column(name = "first_tower_kill", nullable = false)
    private boolean firstTowerKill;

    @Column(name = "game_ended_in_surrender", nullable = false)
    private boolean gameEndedInSurrender;

    @Column(name = "gold_earned", nullable = false)
    private int goldEarned;

    @Column(name = "gold_spent", nullable = false)
    private int goldSpent;

    @Column(name = "inhibitor_take_downs", nullable = false)
    private int inhibitorTakeDowns;

    @Column(name = "item0", nullable = false)
    private int item0;

    @Column(name = "item1", nullable = false)
    private int item1;

    @Column(name = "item2", nullable = false)
    private int item2;

    @Column(name = "item3", nullable = false)
    private int item3;

    @Column(name = "item4", nullable = false)
    private int item4;

    @Column(name = "item5", nullable = false)
    private int item5;

    @Column(name = "item6", nullable = false)
    private int item6;

    @Column(name = "neutral_minions_killed", nullable = false)
    private int neutralMinionsKilled;

    @Column(name = "penta_kills", nullable = false)
    private int pentaKills;

    @Column(name = "quadra_kills", nullable = false)
    private int quadraKills;

    @Column(name = "summoner1_id", nullable = false)
    private int summoner1Id;

    @Column(name = "summoner2_id", nullable = false)
    private int summoner2Id;

    @Column(name = "team_id", nullable = false)
    private int teamId;

    @Column(name = "total_damage_dealt_to_champions", nullable = false)
    private int totalDamageDealtToChampions;

    @Column(name = "total_heal", nullable = false)
    private int totalHeal;

    @Column(name = "total_minions_killed", nullable = false)
    private int totalMinionsKilled;

    @Column(name = "triple_kills", nullable = false)
    private int tripleKills;

    @Column(name = "vision_score", nullable = false)
    private int visionScore;

    @Column(name = "primary_style_id", nullable = false)
    private int primaryStyleId;

    @Column(name = "sub_style_id", nullable = false)
    private int subStyleId;

    @Column(name = "keystone_id", nullable = false)
    private int keystoneId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "perks", nullable = false)
    private PerksDto perks;

    //JPA-Only
    protected Participant() {
    }

    public static Participant from(Long matchId, Long playerId, ParticipantDto dto) {
        Participant p = new Participant();
        p.matchId = matchId;
        p.playerId = playerId;
        p.championId = dto.championId();
        p.teamPosition = dto.teamPosition();
        p.kills = dto.kills();
        p.deaths = dto.deaths();
        p.assists = dto.assists();
        p.win = dto.win();
        p.baronKills = dto.baronKills();
        p.champLevel = dto.champLevel();
        p.doubleKills = dto.doubleKills();
        p.dragonKills = dto.dragonKills();
        p.firstBloodKill = dto.firstBloodKill();
        p.firstTowerKill = dto.firstTowerKill();
        p.gameEndedInSurrender = dto.gameEndedInSurrender();
        p.goldEarned = dto.goldEarned();
        p.goldSpent = dto.goldSpent();
        p.inhibitorTakeDowns = dto.inhibitorTakedowns();
        p.item0 = dto.item0();
        p.item1 = dto.item1();
        p.item2 = dto.item2();
        p.item3 = dto.item3();
        p.item4 = dto.item4();
        p.item5 = dto.item5();
        p.item6 = dto.item6();
        p.neutralMinionsKilled = dto.neutralMinionsKilled();
        p.pentaKills = dto.pentaKills();
        p.quadraKills = dto.quadraKills();
        p.summoner1Id = dto.summoner1Id();
        p.summoner2Id = dto.summoner2Id();
        p.teamId = dto.teamId();
        p.totalDamageDealtToChampions = dto.totalDamageDealtToChampions();
        p.totalHeal = dto.totalHeal();
        p.totalMinionsKilled = dto.totalMinionsKilled();
        p.tripleKills = dto.tripleKills();
        p.visionScore = dto.visionScore();
        p.primaryStyleId = dto.perks().styles().getFirst().style();
        p.subStyleId = dto.perks().styles().getLast().style();
        p.keystoneId = dto.perks().styles().getFirst().selections().getFirst().perk();
        p.perks = dto.perks();
        return p;
    }

    // --- GETTEREK ---
    public Long getId() {
        return id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public int getChampionId() {
        return championId;
    }

    public String getTeamPosition() {
        return teamPosition;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public boolean isWin() {
        return win;
    }

    public int getBaronKills() {
        return baronKills;
    }

    public int getChampLevel() {
        return champLevel;
    }

    public int getDoubleKills() {
        return doubleKills;
    }

    public int getDragonKills() {
        return dragonKills;
    }

    public boolean isFirstBloodKill() {
        return firstBloodKill;
    }

    public boolean isFirstTowerKill() {
        return firstTowerKill;
    }

    public boolean isGameEndedInSurrender() {
        return gameEndedInSurrender;
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public int getGoldSpent() {
        return goldSpent;
    }

    public int getInhibitorTakeDowns() {
        return inhibitorTakeDowns;
    }

    public int getItem0() {
        return item0;
    }

    public int getItem1() {
        return item1;
    }

    public int getItem2() {
        return item2;
    }

    public int getItem3() {
        return item3;
    }

    public int getItem4() {
        return item4;
    }

    public int getItem5() {
        return item5;
    }

    public int getItem6() {
        return item6;
    }

    public int getNeutralMinionsKilled() {
        return neutralMinionsKilled;
    }

    public int getPentaKills() {
        return pentaKills;
    }

    public int getQuadraKills() {
        return quadraKills;
    }

    public int getSummoner1Id() {
        return summoner1Id;
    }

    public int getSummoner2Id() {
        return summoner2Id;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getTotalDamageDealtToChampions() {
        return totalDamageDealtToChampions;
    }

    public int getTotalHeal() {
        return totalHeal;
    }

    public int getTotalMinionsKilled() {
        return totalMinionsKilled;
    }

    public int getTripleKills() {
        return tripleKills;
    }

    public int getVisionScore() {
        return visionScore;
    }

    public int getPrimaryStyleId() {
        return primaryStyleId;
    }

    public int getSubStyleId() {
        return subStyleId;
    }

    public int getKeystoneId() {
        return keystoneId;
    }

    public PerksDto getPerks() {
        return perks;
    }
}
