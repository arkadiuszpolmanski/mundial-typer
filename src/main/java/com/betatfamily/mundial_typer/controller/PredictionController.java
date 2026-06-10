package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.entity.*;
import com.betatfamily.mundial_typer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;
import java.util.Optional;


@Controller
public class PredictionController {

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ConfigRepository configRepository;

    @PostMapping("/predict")
    public String predict(Long matchId,
                          Integer home,
                          Integer away,
                          Authentication auth) {

        Match m = matchRepository.findById(matchId).get();
        User user = userRepository.findByUsername(auth.getName());

        // Blokada
        if (m.getMatchTime().isBefore(LocalDateTime.now())) {
            return "redirect:/matches?error=too-late";
        }

        // Sprawdzamy czy typował
        Optional<Prediction> existing = predictionRepository.findByUserUsernameAndMatchId(auth.getName(), matchId);

        Prediction p = existing.orElse(new Prediction());
        p.setMatch(m);
        p.setUser(user);
        p.setPredictedHome(home);
        p.setPredictedAway(away);

        predictionRepository.save(p);

        return "redirect:/match/" + matchId;
    }

    @GetMapping("/winner")
    public String winnerPage(Model model, Authentication auth) {

        model.addAttribute("countries", countryRepository.findAll());

        TournamentConfig config = configRepository.findById(1L).orElse(null);

        boolean winnerPredictionOpen =
                config == null
                        || config.getWorldCupDeadline() == null
                        || LocalDateTime.now().isBefore(config.getWorldCupDeadline());

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("winnerPredictionOpen", winnerPredictionOpen);

        return "winner";
    }

    @PostMapping("/winner")
    public String saveWinner(Long countryId, Authentication auth) {

        TournamentConfig config = configRepository.findById(1L).orElse(null);

        if (config != null
                && config.getWorldCupDeadline() != null
                && LocalDateTime.now().isAfter(config.getWorldCupDeadline())) {

            return "redirect:/winner?error=too-late";
        }

        User user = userRepository.findByUsername(auth.getName());
        Country country = countryRepository.findById(countryId).orElseThrow();

        user.setWorldCupWinner(country);
        userRepository.save(user);

        return "redirect:/home";
    }
}
