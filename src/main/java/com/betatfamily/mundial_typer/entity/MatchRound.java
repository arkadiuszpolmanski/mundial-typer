package com.betatfamily.mundial_typer.entity;

public enum MatchRound {

    GROUP_R1("Faza grupowa - kolejka 1"),
    GROUP_R2("Faza grupowa - kolejka 2"),
    GROUP_R3("Faza grupowa - kolejka 3"),

    WORLD_CUP_1_16("1/16 finału"),
    WORLD_CUP_1_8("1/8 finału"),
    WORLD_CUP_1_4("Ćwierćfinał"),
    WORLD_CUP_1_2("Półfinał"),

    WORLD_CUP_BRONZE("Mecz o 3 miejsce"),
    WORLD_CUP_FINAL("Finał");

    private final String label;


    MatchRound(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
