package com.betatfamily.mundial_typer.entity;

public enum CupStage {

    OF_16("1/8 finału"),
    QUARTER_FINAL("Ćwierćfinał"),
    SEMI_FINAL("Półfinał"),
    FINAL("Finał");

    private final String label;

    CupStage(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
}
