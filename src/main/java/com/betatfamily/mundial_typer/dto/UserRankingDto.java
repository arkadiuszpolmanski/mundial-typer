package com.betatfamily.mundial_typer.dto;

import com.betatfamily.mundial_typer.entity.Country;
import lombok.Data;

@Data
public class UserRankingDto {

    private String username;
    private String firstName;
    private String lastName;
    private int totalPoints;
    private int correct3;
    private int correct1;
    private Country worldCupWinner;
    private int worldCupWinnerPoints;
}
