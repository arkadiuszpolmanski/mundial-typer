package com.betatfamily.mundial_typer.controller;

import com.betatfamily.mundial_typer.entity.User;
import com.betatfamily.mundial_typer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
