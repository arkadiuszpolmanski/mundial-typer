package com.betatfamily.mundial_typer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Match {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Country homeTeam;

    @ManyToOne
    private Country awayTeam;

    private LocalDateTime matchTime;

    private Integer homeScore;
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    private MatchRound round;
}
