package com.betatfamily.mundial_typer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CupMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CupStage stage;

    private Integer bracketPosition;

    @ManyToOne
    private CupMatch sourceMatch1;

    @ManyToOne
    private CupMatch sourceMatch2;

    @ManyToOne
    private User player1;

    @ManyToOne
    private User player2;

    @ManyToOne
    private User winner;

    @ManyToOne
    private CupSeed player1Seed;

    @ManyToOne
    private CupSeed player2Seed;

    private Integer player1Points;
    private Integer player2Points;
}
