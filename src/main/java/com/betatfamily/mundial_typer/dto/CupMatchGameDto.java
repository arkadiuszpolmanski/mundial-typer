package com.betatfamily.mundial_typer.dto;

import com.betatfamily.mundial_typer.entity.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CupMatchGameDto {

    private Match match;

    private Integer player1Points;
    private Integer player2Points;

    private Integer player1PredictionHome;
    private Integer player1PredictionAway;

    private Integer player2PredictionHome;
    private Integer player2PredictionAway;

    private boolean started;
}
