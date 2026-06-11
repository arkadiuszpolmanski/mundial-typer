package com.betatfamily.mundial_typer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Match match;
    private Integer predictedHome;
    private Integer predictedAway;

    public int getPoints() {

        Match m = this.match;

        if (m.getHomeScore() == null || m.getAwayScore() == null) {
            return 0;
        }

        int realHome = m.getHomeScore();
        int realAway = m.getAwayScore();

        int predHome = this.predictedHome;
        int predAway = this.predictedAway;

        if (realHome == predHome && realAway == predAway) {
            return 3;
        }

        if (Integer.compare(realHome, realAway)
                == Integer.compare(predHome, predAway)) {
            return 1;
        }

        return 0;
    }
}
