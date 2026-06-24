package com.betatfamily.mundial_typer.dto;

import com.betatfamily.mundial_typer.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CupQualificationDto {

    private User user;
    private int points;
    private int correct3;
    private int correct1;
    private Integer leaguePosition;

}
