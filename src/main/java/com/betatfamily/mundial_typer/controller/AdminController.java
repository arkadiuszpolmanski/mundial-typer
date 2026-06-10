package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.entity.*;
import com.betatfamily.mundial_typer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin/world-cup-winner")
    public String worldCupWinnerPage(Model model, Authentication auth) {

        if (!isAdmin(auth)) {
            return "redirect:/home";
        }

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("config", configRepository.findById(1L).orElse(null));
        model.addAttribute("countries", countryRepository.findAll());

        return "admin-world-cup-winner";
    }

    @PostMapping("/admin/world-cup-winner")
    public String setWorldCupWinner(Long countryId) {

        Country country = countryRepository.findById(countryId).orElseThrow();

        TournamentConfig config = configRepository.findById(1L).orElse(new TournamentConfig());

        config.setWorldCupWinner(country);

        configRepository.save(config);

        return "redirect:/admin/world-cup-winner";
    }

    @PostMapping("/admin/set-deadline")
    public String setWorldCupWinner(LocalDateTime deadline) {

        TournamentConfig config = configRepository.findById(1L).orElse(new TournamentConfig());

        config.setWorldCupDeadline(deadline);

        configRepository.save(config);

        return "redirect:/admin/world-cup-winner";
    }

    @GetMapping("/admin")
    public String adminPage(Model model, Authentication auth) {

        if (!isAdmin(auth)) {
            return "redirect:/home";
        }

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("matches", matchRepository.findAllByOrderByMatchTimeAsc());
        model.addAttribute("countries", countryRepository.findAll());

        return "admin";
    }

    @GetMapping("/admin/matches")
    public String adminMatches(Model model,
                               @RequestParam(defaultValue = "active") String type,
                               Authentication auth) {

        if (!isAdmin(auth)) {
            return "redirect:/home";
        }

        List<Match> matches = matchRepository.findAllByOrderByMatchTimeAsc();
        LocalDateTime now = LocalDateTime.now();

        List<Match> filteredMatches = switch (type) {

            case "active" -> matches.stream()
                    .filter(m -> m.getHomeScore() == null && m.getAwayScore() == null)
                    .toList();

            case "live" -> matches.stream()
                    .filter(m -> m.getMatchTime().isBefore(now)
                            && m.getHomeScore() == null)
                    .toList();

            case "finished" -> matches.stream()
                    .filter(m -> m.getHomeScore() != null && m.getAwayScore() != null)
                    .toList();

            default -> matches;
        };

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("countries", countryRepository.findAll());
        model.addAttribute("matches", filteredMatches);
        model.addAttribute("type", type);

        return "admin-matches";
    }

    @PostMapping("/admin/result")
    public String setResult(Long matchId, Integer home, Integer away) {

        Match match = matchRepository.findById(matchId).orElseThrow();

        match.setHomeScore(home);
        match.setAwayScore(away);

        matchRepository.save(match);

        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);

        for (Prediction p: predictions) {

            User user = p.getUser();

            boolean exact = p.getPredictedHome().equals(home) && p.getPredictedAway().equals(away);

            boolean sign = (p.getPredictedHome() - p.getPredictedAway()) * (home - away) > 0; //ten sam zwycięzca

            if (exact) {

                user.setTotalPoints(user.getTotalPoints() + 3);
                user.setCorrect3(user.getCorrect3() + 1);

            } else if (sign) {

                user.setTotalPoints(user.getTotalPoints() + 1);
                user.setCorrect1(user.getCorrect1() + 1);

            } else {

                user.setTotalPoints(user.getTotalPoints());

            }
        }

        userRepository.saveAll(
                predictions.stream()
                        .map(Prediction::getUser)
                        .distinct()
                        .toList());

        return "redirect:/admin/matches";
    }

    @PostMapping("/admin/add-match")
    public String addMatch(@RequestParam Long homeTeamId,
                           @RequestParam Long awayTeamId,
                           @DateTimeFormat(pattern = "yyy-MM-dd'T'HH:mm")
                           LocalDateTime matchTime) {

        Country homeTeam = countryRepository.findById(homeTeamId).orElseThrow();
        Country awayTeam = countryRepository.findById(awayTeamId).orElseThrow();

        Match match = new Match();
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchTime(matchTime);

        matchRepository.save(match);

        return "redirect:/admin/matches";
    }

    @PostMapping("/admin/delete-match")
    public String deleteMatch(@RequestParam Long matchId) {

        predictionRepository.deleteAll(predictionRepository.findByMatchId(matchId));
        matchRepository.deleteById(matchId);
        return "redirect:/admin/matches";
    }

    @GetMapping("/admin/edit/{id}")
    public String editMatch(@PathVariable Long id, Model model, Authentication auth) {

        if (!isAdmin(auth)) {
            return "redirect:/home";
        }

        Match match = matchRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("match", match);
        model.addAttribute("countries", countryRepository.findAll());

        return "edit-match";
    }

    @PostMapping("/admin/edit")
    public String saveEdit(Long id,
                           @RequestParam Long homeTeamId,
                           @RequestParam Long awayTeamId,
                           @DateTimeFormat(pattern = "yyy-MM-dd'T'HH:mm")
                           LocalDateTime matchTime) {

        Country homeTeam = countryRepository.findById(homeTeamId).orElseThrow();
        Country awayTeam = countryRepository.findById(awayTeamId).orElseThrow();

        Match match = matchRepository.findById(id).orElseThrow();

        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setMatchTime(matchTime);

        matchRepository.save(match);

        return "redirect:/admin/edit/" + id;
    }

    @GetMapping("/admin/register")
    public String registerPage(Model model, Authentication auth) {

        if (!isAdmin(auth)) {
            return "redirect:/home";
        }

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);

        return "admin-register";
    }

    @PostMapping("/register")
    public String register(String username,
                           String password,
                           String firstName,
                           String lastName) {

        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setFirstName(firstName);
        u.setLastName(lastName);

        userRepository.save(u);

        return "redirect:/admin/register";
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getName().equals("Arek");
    }
}
