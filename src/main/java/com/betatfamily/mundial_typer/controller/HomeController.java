package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.entity.Match;
import com.betatfamily.mundial_typer.entity.Prediction;
import com.betatfamily.mundial_typer.entity.TournamentConfig;
import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.ConfigRepository;
import com.betatfamily.mundial_typer.repository.MatchRepository;
import com.betatfamily.mundial_typer.repository.PredictionRepository;
import com.betatfamily.mundial_typer.repository.UserRepository;
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

    @GetMapping("/home")
    public String home(Authentication auth, Model model) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusHours(3);

        List<Match> upcomingMatches = matchRepository.findTop4ByMatchTimeAfterOrderByMatchTimeAsc(cutoff);

        Map<Long, Prediction> userPredictions = new HashMap<>();
        if (auth != null) {
            for (Match m : upcomingMatches) {
                predictionRepository
                        .findByUserUsernameAndMatchId(auth.getName(), m.getId())
                        .ifPresent(p -> userPredictions.put(m.getId(), p));
            }
        }

        User user = auth != null ? userRepository.findByUsername(auth.getName()) : null;

        TournamentConfig config = tournamentConfig.findById(1L).orElse(null);

        model.addAttribute("username", auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("matches", upcomingMatches);
        model.addAttribute("userPredictions", userPredictions);
        model.addAttribute("config", config);

        return "home";
    }
}
