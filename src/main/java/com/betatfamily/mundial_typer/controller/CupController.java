package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.dto.CupMatchGameDto;
import com.betatfamily.mundial_typer.dto.CupMatchScoreDto;
import com.betatfamily.mundial_typer.entity.CupMatch;
import com.betatfamily.mundial_typer.entity.CupStage;
import com.betatfamily.mundial_typer.entity.TournamentConfig;
import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.ConfigRepository;
import com.betatfamily.mundial_typer.repository.UserRepository;
import com.betatfamily.mundial_typer.service.CupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CupController {

    @Autowired
    private final CupService cupService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ConfigRepository configRepository;

    @GetMapping("/cup")
    public String cup(Model model,
                      Authentication auth) {

        User user = userRepository.findByUsername(auth.getName());
        TournamentConfig config = configRepository.findById(1L).orElseThrow();

        List<CupMatchScoreDto> matches = cupService.getBracketWithScores();

        List<CupMatchScoreDto> of16 =
                matches.stream()
                        .filter(m -> m.getMatch().getStage() == CupStage.OF_16)
                        .toList();

        List<CupMatchScoreDto> quarterFinals =
                matches.stream()
                        .filter(m -> m.getMatch().getStage() == CupStage.QUARTER_FINAL)
                        .toList();

        List<CupMatchScoreDto> semiFinals =
                matches.stream()
                        .filter(m -> m.getMatch().getStage() == CupStage.SEMI_FINAL)
                        .toList();

        List<CupMatchScoreDto> finals =
                matches.stream()
                        .filter(m -> m.getMatch().getStage() == CupStage.FINAL)
                        .toList();

        CupStage activeStage = CupStage.OF_16;

        if (!finals.isEmpty()) {
            activeStage = CupStage.FINAL;
        } else if (!semiFinals.isEmpty()) {
            activeStage = CupStage.SEMI_FINAL;
        } else if (!quarterFinals.isEmpty()) {
            activeStage = CupStage.QUARTER_FINAL;
        }

        boolean bracketVisible = config.getBracketVisible();
        boolean bracketActive = bracketVisible && !matches.isEmpty();

        model.addAttribute("user", user);
        model.addAttribute("bracketVisible", bracketVisible);
        model.addAttribute("bracketActive", bracketActive);
        model.addAttribute("activeStage", activeStage);
        model.addAttribute("ranking",
                bracketActive
                ? cupService.getFrozenQualificationRanking()
                : cupService.getQualificationRanking());
        model.addAttribute("matches", of16);
        model.addAttribute("quarterFinals", quarterFinals);
        model.addAttribute("semiFinals", semiFinals);
        model.addAttribute("finals", finals);

        return "cup";
    }

    @GetMapping("/cup/match/{id}")
    public String cupMatch(
            @PathVariable Long id,
            Model model,
            Authentication auth) {


        User user = userRepository.findByUsername(auth.getName());

        CupMatch match = cupService.getCupMatch(id);
        CupMatchScoreDto dto = cupService.getCupMatchDetails(id);
        List<CupMatchGameDto> games = cupService.getCupGames(dto.getMatch());


        model.addAttribute("user", user);
        model.addAttribute("match", match);
        model.addAttribute("cupMatch", dto);
        model.addAttribute("games", games);


        return "cup-match";
    }
}
