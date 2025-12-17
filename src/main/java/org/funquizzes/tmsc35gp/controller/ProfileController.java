package org.funquizzes.tmsc35gp.controller;

import org.funquizzes.tmsc35gp.dto.ChangePasswordDto;
import org.funquizzes.tmsc35gp.dto.UpdateProfileDto;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.QuizRating;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.entity.UserStatistic;
import org.funquizzes.tmsc35gp.service.QuizRatingService;
import org.funquizzes.tmsc35gp.service.QuizService;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/users/profile")
public class ProfileController {

    @Autowired
    private UserService userService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private QuizRatingService quizRatingService;


    @GetMapping
    public String profileRedirect() {
        return "redirect:/users/profile/main?tab=overview";
    }

    @GetMapping("/main")
    public String profileMain(Authentication authentication,
                              Model model,
                              @RequestParam(defaultValue = "overview") String tab,
                              @RequestParam(required = false) String message,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {

        User user = (User) userService.loadUserByUsername(authentication.getName());

        model.addAttribute("user", user);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", tab);

        if (message != null) {
            model.addAttribute("message", java.net.URLDecoder.decode(message, StandardCharsets.UTF_8));
        }

        // Загружаем данные в зависимости от активной вкладки
        switch (tab) {
            case "overview":
                break;

            case "statistic":
                UserStatistic statistic = userService.getUserStatistics(authentication.getName());
                model.addAttribute("statistic", statistic);

                List<UserStatistic> topPlayers = userService.getTopUsersByScore(10);
                model.addAttribute("topPlayers", topPlayers);

                // Рассчитываем проценты если есть данные
                if (statistic.getTotalQuizzesPlayed() > 0) {
                    int accuracy = (statistic.getTotalCorrectAnswers() * 100) / (statistic.getTotalQuizzesPlayed() * 10);
                    model.addAttribute("accuracy", accuracy);
                }
                break;

            case "edit":
                UpdateProfileDto updateProfileDto = new UpdateProfileDto();
                updateProfileDto.setName(user.getName());
                updateProfileDto.setUsername(user.getUsername());
                updateProfileDto.setEmail(user.getEmail());
                updateProfileDto.setBiography(user.getBiography());
                updateProfileDto.setPublicProfile(user.isPublicProfile());
                model.addAttribute("updateProfileDto", updateProfileDto);
                break;

            case "change-password":
                model.addAttribute("passwordDto", new ChangePasswordDto());
                break;

            case "friends":
                // Логика для вкладки друзей
                break;

            case "settings":
                // Логика для настроек
                break;

            case "achievements":
                // Логика для достижений
                break;

            case "activity":
                // загружаем оценки пользователя
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<QuizRating> ratingsPage = quizRatingService.getUserRatings(user, pageable);

                model.addAttribute("ratings", ratingsPage.getContent());
                model.addAttribute("totalPages", ratingsPage.getTotalPages());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalRatings", ratingsPage.getTotalElements());
                model.addAttribute("hasRatings", !ratingsPage.isEmpty());
                break;

            case "game-history":
                // Логика для истории игр
                break;

            case "my-quizzes":
                // Загружаем викторины пользователя
                List<Quiz> quizzes = quizService.findByCreator(user);
                model.addAttribute("quizzes", quizzes);

                // Подсчет статистики викторин
                int publicCount = 0;
                int privateCount = 0;
                for (Quiz quiz : quizzes) {
                    if (quiz.isPublic()) {
                        publicCount++;
                    } else {
                        privateCount++;
                    }
                }

                model.addAttribute("publicCount", publicCount);
                model.addAttribute("privateCount", privateCount);
                break;
        }

        return "profile/main";
    }

    // Обработка редактирования профиля
    @PostMapping("/edit")
    public String profileEdit(@ModelAttribute("updateProfileDto") UpdateProfileDto dto,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(authentication.getName(), dto);
            String encodedMessage = URLEncoder.encode("Информация профиля успешно обновлена!", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=overview&message=" + encodedMessage;
        } catch (Exception e) {
            String encodedMessage = URLEncoder.encode("Ошибка при обновлении профиля!", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=edit&message=" + encodedMessage;
        }
    }

    // Обработка смены пароля
    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute ChangePasswordDto dto,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        boolean success = userService.changePassword(authentication.getName(), dto);
        if (success) {
            String encodedMessage = URLEncoder.encode("Пароль успешно изменён", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=overview&message=" + encodedMessage;
        } else {
            String encodedMessage = URLEncoder.encode("Неверный текущий пароль или пароли не совпадают", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=change-password&message=" + encodedMessage;
        }
    }

    // Обработка уведомлений
    @PostMapping("/settings/notifications")
    public String toggleNotifications(@RequestParam boolean receiveNotifications,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        user.setReceiveNotifications(receiveNotifications);

        UpdateProfileDto updateDto = new UpdateProfileDto();
        updateDto.setName(user.getName());
        updateDto.setUsername(user.getUsername());
        updateDto.setEmail(user.getEmail());
        updateDto.setBiography(user.getBiography());
        updateDto.setPublicProfile(user.isPublicProfile());

        userService.updateProfile(authentication.getName(), updateDto);

        String encodedMessage = URLEncoder.encode("Настройки уведомлений обновлены", StandardCharsets.UTF_8);
        return "redirect:/users/profile/main?tab=settings&message=" + encodedMessage;
    }

    // Добавление друга
    @PostMapping("/friends/add/{friendId}")
    public String addFriend (@PathVariable Long friendId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.addFriend(authentication.getName(), friendId);
            String encodedMessage = URLEncoder.encode("Пользователь добавлен в Друзья", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=friends&message=" + encodedMessage;
        } catch (Exception e) {
            String encodedMessage = URLEncoder.encode("Ошибка при добавлении пользователя в Друзья", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=friends&message=" + encodedMessage;
        }
    }

    // Удаление друга
    @PostMapping("/friends/remove/{friendId}")
    public String removeFriend (@PathVariable Long friendId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.removeFriend(authentication.getName(), friendId);
            String encodedMessage = URLEncoder.encode("Пользователь удалён из списка друзей", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=friends&message=" + encodedMessage;
        } catch (Exception e) {
            String encodedMessage = URLEncoder.encode("Ошибка при удалении пользователя из списка друзей", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=friends&message=" + encodedMessage;
        }
    }
}