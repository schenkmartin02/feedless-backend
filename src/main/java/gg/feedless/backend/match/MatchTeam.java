package gg.feedless.backend.match;

import gg.feedless.backend.riot.dto.match.TeamDto;
import jakarta.persistence.*;

@Entity
@Table(name = "match_teams")
public class MatchTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "team_id", nullable = false)
    private int teamId;

    @Column(name = "win", nullable = false)
    private boolean win;

    @Column(name = "barons", nullable = false)
    private int barons;

    @Column(name = "dragons", nullable = false)
    private int dragons;

    @Column(name = "heralds", nullable = false)
    private int heralds;

    @Column(name = "towers", nullable = false)
    private int towers;

    @Column(name = "inhibitors", nullable = false)
    private int inhibitors;

    //JPA-Only
    protected MatchTeam() {}

    public static MatchTeam from(Long matchId, TeamDto teamDto) {
        MatchTeam m = new MatchTeam();
        m.matchId = matchId;
        m.barons = teamDto.objectives().baron().kills();
        m.dragons = teamDto.objectives().dragon().kills();
        m.heralds = teamDto.objectives().riftHerald().kills();
        m.inhibitors = teamDto.objectives().inhibitor().kills();
        m.towers = teamDto.objectives().tower().kills();
        m.teamId = teamDto.teamId();
        m.win = teamDto.win();
        return m;
    }

}
