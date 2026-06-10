package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.entity.Country;
import com.betatfamily.mundial_typer.entity.Match;
import com.betatfamily.mundial_typer.entity.Prediction;
import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.CountryRepository;
import com.betatfamily.mundial_typer.repository.MatchRepository;
import com.betatfamily.mundial_typer.repository.PredictionRepository;
import com.betatfamily.mundial_typer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CountryRepository countryRepository;

    @GetMapping("/matches")
    public String matches(Model model,
                          Authentication auth,
                          @RequestParam(defaultValue = "active") String type) {

        List<Match> matches = matchRepository.findAllByOrderByMatchTimeAsc();
        boolean isLogged = auth != null;

        Map<Long, Prediction> userPredictions = new HashMap<>();
        Map<Long, String> matchStatus = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();

        if (isLogged) {
            for (Match m : matches) {
                predictionRepository
                        .findByUserUsernameAndMatchId(auth.getName(), m.getId())
                        .ifPresent(p -> userPredictions.put(m.getId(), p));
            }
        }

        for (Match m : matches) {

            if (m.getHomeScore() != null && m.getAwayScore() != null) {
                matchStatus.put(m.getId(), "FINISHED");
            } else if (m.getMatchTime().isBefore(now)) {
                matchStatus.put(m.getId(), "LIVE");
            } else {
                matchStatus.put(m.getId(), "UPCOMING");
            }
        }

        List<Match> filteredMatches = switch (type) {

            case "finished" -> matches.stream()
                    .filter(m -> m.getHomeScore() != null && m.getAwayScore() != null)
                    .toList();

            case "active" -> matches.stream()
                    .filter(m -> m.getHomeScore() == null && m.getAwayScore() == null)
                    .toList();

            case "live" -> matches.stream().filter(m -> m.getMatchTime().isBefore(now)
                    && m.getHomeScore() == null && m.getAwayScore() == null)
                    .toList();

            case "all" -> matches;

            default -> matches;
        };

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("matches", filteredMatches);
        model.addAttribute("userPredictions", userPredictions);
        model.addAttribute("now", now);
        model.addAttribute("matchStatus", matchStatus);
        model.addAttribute("type", type);

        return "matches";
    }

    @GetMapping("/match/{id}")
    public String matchDetails(@PathVariable Long id,
                               Model model,
                               Authentication auth) {

        Match match = matchRepository.findById(id).orElseThrow();

        List<Prediction> predictions = predictionRepository.findByMatchId(id);

        Prediction myPrediction = null;

        if (auth != null) {
            myPrediction =
                    predictionRepository
                            .findByUserUsernameAndMatchId(auth.getName(), id)
                            .orElse(null);
        }

        boolean started = match.getMatchTime().isBefore(LocalDateTime.now());

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("match", match);
        model.addAttribute("started", started);
        model.addAttribute("predictions", predictions);
        model.addAttribute("myPrediction", myPrediction);

        return "match";
    }

    @GetMapping("/finish/{id}")
    @ResponseBody
    public String finish(@PathVariable Long id,
                         Integer home,
                         Integer away) {
        Match m = matchRepository.findById(id).orElseThrow();

        m.setHomeScore(home);
        m.setAwayScore(away);

        matchRepository.save(m);

        return "OK - wynik zapisany";
    }
}
