package org.funquizzes.tmsc35gp.controller;

import org.funquizzes.tmsc35gp.dto.CreateUserDto;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/sing-up")
    public String users() {
        return "singUp";
    }

    @PostMapping("/sing-up")
    public String userSubmit(CreateUserDto dto) {

        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());

        userService.create(user);

        return "redirect:/";
    }

    @GetMapping("/log-in")
    public String login() {
        return "login";
    }
}
