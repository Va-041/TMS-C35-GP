package org.funquizzes.tmsc35gp.configuration;

import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(customizer -> customizer
                        // Публичные пути
                        .requestMatchers(
                                "/",
                                "/users/sing-up",
                                "/users/log-in",
                                "/users/profile/view/**", // публичные профили
                                "/quizzes",
                                "/quizzes/**",
                                "/quizzes/details/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/fragments/**"
                        )
                        .permitAll()
                        // Только для авторизованных
                        .requestMatchers(
                                "/users/profile/**",
                                "/users/profile/main/**",
                                "/quizzes/create",
                                "/quizzes/edit/**",
                                "/quizzes/delete/**",
                                "/quizzes/my",
                                "/quizzes/play/**",
                                "/quizzes/play/*/question/**",
                                "/quizzes/play/*/skip/**",
                                "/quizzes/play/*/results"
                        )
                        .authenticated()
                        // Остальные пути тоже требуют авторизации
                        .anyRequest()
                        .authenticated())
                .formLogin(customizer -> customizer
                        .loginPage("/users/log-in")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/users/log-in?error=true")
                        .permitAll())
                .logout(customizer -> customizer
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/users/log-in?expired=true")
                        .maximumSessions(1)
                        .expiredUrl("/users/log-in?expired=true"))
                .build();
    }

    @Autowired
    private UserService userService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(11));
        provider.setUserDetailsService(userService);

        return provider;
    }
}