package com.betatfamily.mundial_typer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    private String firstName;
    private String lastName;

    private Integer totalPoints = 0;
    private Integer correct3 = 0;
    private Integer correct1 = 0;

    @ManyToOne
    private Country worldCupWinner;
}
