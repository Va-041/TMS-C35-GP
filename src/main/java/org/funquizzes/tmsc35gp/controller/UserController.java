package org.funquizzes.tmsc35gp.controller;

import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/users")
public class UserController {

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/sing-up")
    public String showRegistrationForm(Model model) {
        return "singUp";
    }

    @PostMapping("/sing-up")
    public String registerUser(HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        String name = request.getParameter("name");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        logger.info("=== РЕГИСТРАЦИЯ (НАЧАЛО) ===");
        logger.info("Имя: {}", name);
        logger.info("Username: {}", username);
        logger.info("Пароль: {}", (password != null ? "предоставлен" : "null"));
        logger.info("Подтверждение: {}", confirmPassword);

        // Простая валидация
        if (name == null || name.trim().isEmpty() || name.length() < 2) {
            logger.warn("Валидация: имя не прошло");
            redirectAttributes.addFlashAttribute("error", "Имя должно содержать минимум 2 символа");
            return "redirect:/users/sing-up";
        }

        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            logger.warn("Валидация: username не прошел");
            redirectAttributes.addFlashAttribute("error", "Имя пользователя должно содержать минимум 3 символа");
            return "redirect:/users/sing-up";
        }

        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            logger.warn("Валидация: username содержит недопустимые символы");
            redirectAttributes.addFlashAttribute("error", "Имя пользователя может содержать только буквы, цифры и символ подчеркивания");
            return "redirect:/users/sing-up";
        }

        if (password == null || password.length() < 6) {
            logger.warn("Валидация: пароль слишком короткий");
            redirectAttributes.addFlashAttribute("error", "Пароль должен содержать минимум 6 символов");
            return "redirect:/users/sing-up";
        }

        if (!password.equals(confirmPassword)) {
            logger.warn("Валидация: пароли не совпадают");
            redirectAttributes.addFlashAttribute("error", "Пароли не совпадают");
            return "redirect:/users/sing-up";
        }

        // Проверка существования пользователя
        logger.info("Проверка существования пользователя: {}", username);
        if (userService.findByUsername(username).isPresent()) {
            logger.warn("Пользователь уже существует: {}", username);
            redirectAttributes.addFlashAttribute("error", "Пользователь с таким именем уже существует");
            return "redirect:/users/sing-up";
        }

        logger.info("Пользователь не существует, можно создавать");

        try {
            logger.info("=== СОЗДАНИЕ ОБЪЕКТА USER ===");
            User user = new User();
            user.setName(name);
            user.setUsername(username);
            user.setPassword(password); // Пароль будет зашифрован в userService.create()
            // НЕ устанавливаем email - он может быть null

            logger.info("User создан: {}", user.getUsername());
            logger.info("Вызов userService.create()...");

            userService.create(user); // ВАЖНО: вызываем метод create()

            logger.info("=== ПОЛЬЗОВАТЕЛЬ СОЗДАН УСПЕШНО ===");
            logger.info("Username: {}", username);
            logger.info("Name: {}", name);

            redirectAttributes.addFlashAttribute("successMessage", "Регистрация прошла успешно! Теперь вы можете войти.");
            return "redirect:/users/log-in";

        } catch (Exception e) {
            logger.error("=== ИСКЛЮЧЕНИЕ В РЕГИСТРАЦИИ ===", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "redirect:/users/sing-up";
        }
    }

    @GetMapping("/log-in")
    public String showLoginForm() {
        return "login";
    }

    // для статуса с онлайн игроками - режим пульсации (проверка что пользователь залогинен каждую минуту)
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            userService.updateLastActivity(username);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }
}