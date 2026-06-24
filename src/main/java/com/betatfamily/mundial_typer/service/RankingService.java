package com.betatfamily.mundial_typer.service;

import com.betatfamily.mundial_typer.dto.UserRankingDto;
import com.betatfamily.mundial_typer.entity.Prediction;
import com.betatfamily.mundial_typer.entity.TournamentConfig;
import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.ConfigRepository;
import com.betatfamily.mundial_typer.repository.PredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingService {

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private PointsService pointsService;

//    public int calculateMatchPoints(Prediction p) {
//
//        Match m = p.getMatch();
//
//        if (m.getHomeScore() == null || m.getAwayScore() == null) {
//            return 0; // mecz jeszcze nie rozegrany
//        }
//
//        int realHome = m.getHomeScore();
//        int realAway = m.getAwayScore();
//
//        int predHome = p.getPredictedHome();
//        int predAway = p.getPredictedAway();
//
//        // dokładny wynik
//        if (realHome == predHome && realAway == predAway) {
//            return 3;
//        }
//
//        // poprawny typ
//        if (Integer.compare(realHome, realAway) == Integer.compare(predHome, predAway)) {
//            return 1;
//        }
//
//        return 0;
//    }

    public List<UserRankingDto> buildRanking(List<User> users,
                                             List<Prediction> predictions) {

        TournamentConfig config = configRepository.findById(1L).orElse(null);

        Map<String, UserRankingDto> map = new HashMap<>();

        for (User u : users) {

            UserRankingDto dto = new UserRankingDto();
            dto.setUsername(u.getUsername());
            dto.setFirstName(u.getFirstName());
            dto.setLastName(u.getLastName());
            dto.setTotalPoints(0);
            dto.setCorrect3(0);
            dto.setCorrect1(0);
            dto.setWorldCupWinnerPoints(0);
            dto.setId(u.getId());

            if (u.getWorldCupWinner() != null) {
                dto.setWorldCupWinner(u.getWorldCupWinner());
            }

            map.put(u.getUsername(), dto);
        }

        for (Prediction p : predictions) {

            UserRankingDto dto = map.get(p.getUser().getUsername());

            int pts = pointsService.calculateMatchPoints(p);

            dto.setTotalPoints(dto.getTotalPoints() + pts);

            if (pts == 3) dto.setCorrect3(dto.getCorrect3() + 1);
            if (pts == 1) dto.setCorrect1(dto.getCorrect1() + 1);
        }

        for (User u : users) {

            UserRankingDto dto = map.get(u.getUsername());

            int wcPoints = calculateWorldCupPoints(u, config);

            dto.setWorldCupWinnerPoints(wcPoints);
            dto.setTotalPoints(dto.getTotalPoints() + wcPoints);
        }

        return map.values()
                .stream()
                .sorted(Comparator
                        .comparingInt(UserRankingDto::getTotalPoints).reversed()
                        .thenComparing(UserRankingDto::getWorldCupWinnerPoints, Comparator.reverseOrder())
                        .thenComparing(UserRankingDto::getCorrect3, Comparator.reverseOrder())
                        .thenComparing(UserRankingDto::getCorrect1, Comparator.reverseOrder())
                        .thenComparing(UserRankingDto::getFirstName)
                        .thenComparing(UserRankingDto::getLastName)
        ).toList();
    }

    public int calculateWorldCupPoints(User user, TournamentConfig config) {

        if (user.getWorldCupWinner() == null || config.getWorldCupWinner() == null) {
            return 0;
        }

        return user.getWorldCupWinner().getId().equals(config.getWorldCupWinner().getId()) ? 5 : 0;
    }

    public int calculateTotalPoints(User user) {

        int matchPoints = predictionRepository.findByUser(user)
                .stream()
                .mapToInt(pointsService::calculateMatchPoints)
                .sum();

        int worldCupWinnerPoints = calculateWorldCupPoints(user, configRepository.findById(1L).orElse(null));

        return matchPoints + worldCupWinnerPoints;
    }
}
