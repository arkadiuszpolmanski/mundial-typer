package com.betatfamily.mundial_typer.dto;

import com.betatfamily.mundial_typer.entity.CupMatch;
import com.betatfamily.mundial_typer.entity.CupStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CupMatchScoreDto {

    private CupMatch match;

    private int player1Points;
    private int player2Points;

    private List<CupMatchGameDto> games;

    public CupMatchScoreDto(
            CupMatch match,
            int player1Points,
            int player2Points
    ) {
        this.match = match;
        this.player1Points = player1Points;
        this.player2Points = player2Points;
        this.games = null;
    }

    public CupStage getStage() {
        return match.getStage();
    }
}
