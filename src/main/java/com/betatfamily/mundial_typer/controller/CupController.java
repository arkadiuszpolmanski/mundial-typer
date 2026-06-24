package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.entity.CupMatch;
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

        model.addAttribute("user", user);
        model.addAttribute("ranking", cupService.getQualificationRanking());
        model.addAttribute("matches", cupService.getBracket());
        model.addAttribute("bracketVisible", config.getBracketVisible());

        return "cup";
    }

    @GetMapping("/cup/match/{id}")
    public String cupMatch(
            @PathVariable Long id,
            Model model,
            Authentication auth) {


        User user =
                userRepository.findByUsername(auth.getName());


        CupMatch match = cupService.getCupMatch(id);


        model.addAttribute("user", user);
        model.addAttribute("match", match);


        return "cup-match";
    }
}
