package org.funquizzes.tmsc35gp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/")
public class IndexPageController {

    @GetMapping
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            log.info("User logged in: {}, Authorities: {}",
                    auth.getName(), auth.getAuthorities());
        } else {
            log.info("No user logged in");
        }

        return "index";
    }
}
