package org.funquizzes.tmsc35gp.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class indexPageController {

    @GetMapping
    public String index() {

        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return "index";
    }
}
