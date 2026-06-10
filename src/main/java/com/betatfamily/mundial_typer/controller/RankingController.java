package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.dto.UserRankingDto;
import com.betatfamily.mundial_typer.entity.Match;
import com.betatfamily.mundial_typer.entity.Prediction;
import com.betatfamily.mundial_typer.entity.TournamentConfig;
import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.ConfigRepository;
import com.betatfamily.mundial_typer.repository.MatchRepository;
import com.betatfamily.mundial_typer.repository.PredictionRepository;
import com.betatfamily.mundial_typer.repository.UserRepository;
import com.betatfamily.mundial_typer.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RankingController {

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigRepository configRepository;

    @GetMapping("/ranking")
    public String ranking(Model model, Authentication auth) {

        List<User> users = userRepository.findAll();
        List<Prediction> predictions = predictionRepository.findAll();
        List<UserRankingDto> ranking = rankingService.buildRanking(users, predictions);

        TournamentConfig config = configRepository.findById(1L).orElse(null);
        LocalDateTime deadline = config != null ? config.getWorldCupDeadline() : null;

        boolean showWorldCupWinner = deadline != null && LocalDateTime.now().isAfter(deadline);

        User user = userRepository.findByUsername(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("ranking", ranking);
        model.addAttribute("showWorldCupWinner", showWorldCupWinner);

        return "ranking";
    }
}
