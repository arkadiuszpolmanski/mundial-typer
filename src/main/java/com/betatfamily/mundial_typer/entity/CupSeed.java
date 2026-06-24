package com.betatfamily.mundial_typer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CupSeed {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private Integer seed;
    private Integer qualificationPoints;
    private Integer correct3;
    private Integer correct1;
    private Integer leaguePosition;

    private boolean eliminated;

}
