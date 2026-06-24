package com.betatfamily.mundial_typer.service;

import com.betatfamily.mundial_typer.entity.Match;
import com.betatfamily.mundial_typer.entity.MatchRound;
import com.betatfamily.mundial_typer.entity.Prediction;
import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.PredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointsService {

    @Autowired
    private PredictionRepository predictionRepository;

    public int calculateMatchPoints(Prediction p) {

        Match m = p.getMatch();

        if (m.getHomeScore() == null || m.getAwayScore() == null) {
            return 0; // mecz jeszcze nie rozegrany
        }

        int realHome = m.getHomeScore();
        int realAway = m.getAwayScore();

        int predHome = p.getPredictedHome();
        int predAway = p.getPredictedAway();

        // dokładny wynik
        if (realHome == predHome && realAway == predAway) {
            return 3;
        }

        // poprawny typ
        if (Integer.compare(realHome, realAway) == Integer.compare(predHome, predAway)) {
            return 1;
        }

        return 0;
    }

    public int calculateCupQualificationPoints(User user) {

        return predictionRepository
                .findByUser(user)
                .stream()
                .filter(p -> p.getMatch().getRound() == MatchRound.GROUP_R3)
                .mapToInt(this::calculateMatchPoints)
                .sum();
    }

    public int countCorrect3(User user) {

        return predictionRepository
                .findByUser(user)
                .stream()
                .filter(p -> p.getMatch().getRound() == MatchRound.GROUP_R3)
                .filter(p -> calculateMatchPoints(p) == 3)
                .toList()
                .size();
    }

    public int countCorrect1(User user) {

        return predictionRepository
                .findByUser(user)
                .stream()
                .filter(p -> p.getMatch().getRound() == MatchRound.GROUP_R3)
                .filter(p -> calculateMatchPoints(p) == 1)
                .toList()
                .size();
    }
}
