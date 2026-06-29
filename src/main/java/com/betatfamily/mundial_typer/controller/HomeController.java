package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.dto.CupMatchScoreDto;
import com.betatfamily.mundial_typer.entity.*;
import com.betatfamily.mundial_typer.repository.ConfigRepository;
import com.betatfamily.mundial_typer.repository.MatchRepository;
import com.betatfamily.mundial_typer.repository.PredictionRepository;
import com.betatfamily.mundial_typer.repository.UserRepository;
import com.betatfamily.mundial_typer.service.CupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigRepository tournamentConfig;

    @Autowired
    private CupService cupService;

    @GetMapping("/home")
    public String home(Authentication auth, Model model) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusHours(3);

        List<Match> upcomingMatches = matchRepository.findTop6ByMatchTimeAfterOrderByMatchTimeAsc(cutoff);

        Map<Long, Prediction> userPredictions = new HashMap<>();
        if (auth != null) {
            for (Match m : upcomingMatches) {
                predictionRepository
                        .findByUserUsernameAndMatchId(auth.getName(), m.getId())
                        .ifPresent(p -> userPredictions.put(m.getId(), p));
            }
        }

        Map<Long, String> matchStatus = new HashMap<>();
        for (Match m : upcomingMatches) {

            if (m.getHomeScore() != null && m.getAwayScore() != null) {
                matchStatus.put(m.getId(), "FINISHED");
            } else if (m.getMatchTime().isBefore(now)) {
                matchStatus.put(m.getId(), "LIVE");
            } else {
                matchStatus.put(m.getId(), "UPCOMING");
            }
        }

        User user = auth != null ? userRepository.findByUsername(auth.getName()) : null;

        TournamentConfig config = tournamentConfig.findById(1L).orElse(null);

        CupMatch cupMatch = cupService.getUserCupMatch(user);
        CupMatchScoreDto cupMatchScore;

        if(cupMatch != null) {
            cupMatchScore = cupService.getCupMatchDetails(cupMatch.getId());

            boolean userIsPlayer2 = cupMatch.getPlayer2().getId().equals(user.getId());

            boolean cupEliminated =
                    cupMatch.getWinner() != null &&
                    !cupMatch.getWinner().getId().equals(user.getId());

            boolean cupChampion =
                    cupMatch.getWinner() != null &&
                    cupMatch.getStage() == CupStage.FINAL &&
                    cupMatch.getWinner().getId().equals(user.getId());

            model.addAttribute("cupMatch", cupMatchScore);
            model.addAttribute("reverseCupScore", userIsPlayer2);
            model.addAttribute("cupEliminated", cupEliminated);
            model.addAttribute("cupChampion", cupChampion);
        }



        model.addAttribute("matchStatus", matchStatus);
        model.addAttribute("username", auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("matches", upcomingMatches);
        model.addAttribute("userPredictions", userPredictions);
        model.addAttribute("config", config);
        model.addAttribute("cupVisible", config.getCupVisible());

        return "home";
    }
}
