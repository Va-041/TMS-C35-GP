package org.funquizzes.tmsc35gp.controller;

import jakarta.validation.Valid;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/users/profile")
public class ProfileController {

    @Autowired
    private UserService userService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private QuizRatingService quizRatingService;

    // для Windows/Linux совместимости:
    private static final String UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "avatars").toString();

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

        // обновление статистики пользователя перед отображением
        UserStatistic statistic = userService.getUserStatistics(authentication.getName());
        user.setStatistic(statistic);

        // ВСЕГДА добавляем статистику в модель для сайдбара
        model.addAttribute("statistic", statistic);

        model.addAttribute("user", user);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", tab);

        if (message != null) {
            try {
                model.addAttribute("message", java.net.URLDecoder.decode(message, StandardCharsets.UTF_8));
            } catch (Exception e) {
                model.addAttribute("message", message);
            }
        }

        // Загружаем данные в зависимости от активной вкладки
        switch (tab) {
            case "overview":
                // Рассчитываем проценты
                if (statistic.getTotalQuizzesPlayed() > 0) {
                    int totalPossibleAnswers = statistic.getTotalQuizzesPlayed() * 10;
                    int accuracy = (statistic.getTotalCorrectAnswers() * 100) / Math.max(totalPossibleAnswers, 1);
                    model.addAttribute("accuracy", Math.min(accuracy, 100));
                } else {
                    model.addAttribute("accuracy", 0);
                }
                break;

            case "statistic":
                List<UserStatistic> topPlayers = userService.getTopUsersByScore(10);
                model.addAttribute("topPlayers", topPlayers);

                // Рассчитываем максимальный счет для прогресс-баров
                int maxScore = topPlayers.stream()
                        .mapToInt(UserStatistic::getTotalScore)
                        .max()
                        .orElse(1);
                model.addAttribute("maxScore", maxScore);

                // Рассчитываем проценты
                if (statistic.getTotalQuizzesPlayed() > 0) {
                    int totalPossibleAnswers = statistic.getTotalQuizzesPlayed() * 10;
                    int accuracy = (statistic.getTotalCorrectAnswers() * 100) / Math.max(totalPossibleAnswers, 1);
                    model.addAttribute("accuracy", Math.min(accuracy, 100));
                } else {
                    model.addAttribute("accuracy", 0);
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

            case "my-quizzes":
                // Загружаем викторины пользователя
                List<Quiz> quizzes = quizService.findByCreator(user);
                model.addAttribute("quizzes", quizzes);

                // Подсчет статистики викторин
                int publicCount = 0;
                int privateCount = 0;
                int totalPlays = 0;
                double totalRating = 0.0;
                int ratedQuizzes = 0;

                for (Quiz quiz : quizzes) {
                    if (quiz.isPublic()) {
                        publicCount++;
                    } else {
                        privateCount++;
                    }

                    // Суммируем статистику викторин
                    if (quiz.getPlaysCount() != null) {
                        totalPlays += quiz.getPlaysCount();
                    }
                    if (quiz.getAverageRating() != null && quiz.getAverageRating() > 0) {
                        totalRating += quiz.getAverageRating();
                        ratedQuizzes++;
                    }
                }

                model.addAttribute("publicCount", publicCount);
                model.addAttribute("privateCount", privateCount);
                model.addAttribute("totalPlays", totalPlays);
                model.addAttribute("averageRating", ratedQuizzes > 0 ? String.format("%.1f", totalRating / ratedQuizzes) : "0.0");
                break;
        }

        return "profile/main";
    }

    // Обработка редактирования профиля с загрузкой аватарки
    @PostMapping("/edit")
    public String profileEdit(@Valid @ModelAttribute UpdateProfileDto updateDto,
                              BindingResult bindingResult,
                              @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        // Проверка валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "edit");
            model.addAttribute("updateProfileDto", updateDto);

            // Загружаем пользователя для отображения
            User user = (User) userService.loadUserByUsername(authentication.getName());
            model.addAttribute("user", user);

            return "profile/main";
        }

        try {
            User user = (User) userService.loadUserByUsername(authentication.getName());

            // Проверка уникальности username
            Optional<User> existingUser = userService.findByUsername(updateDto.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                bindingResult.rejectValue("username", "error.username", "Этот username уже занят");
                model.addAttribute("activeTab", "edit");
                model.addAttribute("updateProfileDto", updateDto);
                model.addAttribute("user", user);
                return "profile/main";
            }

            // Проверка уникальности email
            existingUser = userService.findByEmail(updateDto.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                bindingResult.rejectValue("email", "error.email", "Этот email уже используется");
                model.addAttribute("activeTab", "edit");
                model.addAttribute("updateProfileDto", updateDto);
                model.addAttribute("user", user);
                return "profile/main";
            }

            // Сохраняем аватарку, если она загружена
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = handleAvatarUpload(avatarFile, user.getUsername());
                updateDto.setAvatarUrl(avatarUrl);
            }

            userService.updateProfile(authentication.getName(), updateDto);

            String encodedMessage = URLEncoder.encode("Информация профиля успешно обновлена!", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=overview&message=" + encodedMessage;
        } catch (Exception e) {
            e.printStackTrace();
            String encodedMessage = URLEncoder.encode("Ошибка при обновлении профиля: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=edit&message=" + encodedMessage;
        }
    }

    // Метод для загрузки аватарки
    private String handleAvatarUpload(MultipartFile file, String username) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Проверяем тип файла
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Пожалуйста, загрузите изображение");
        }

        // Проверяем размер файла (максимум 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }

        // Создаем директорию, если она не существует
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генерируем уникальное имя файла
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = username + "_" + UUID.randomUUID().toString() + extension;

        // Сохраняем файл
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        // Возвращаем относительный путь для сохранения в БД
        // Путь должен начинаться с /uploads/ для доступа через браузер
        return "/uploads/avatars/" + filename;
    }

    // Обработка смены пароля
    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        // Проверка валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "change-password");
            model.addAttribute("passwordDto", dto);

            // Загружаем пользователя
            User user = (User) userService.loadUserByUsername(authentication.getName());
            model.addAttribute("user", user);

            return "profile/main";
        }

        // Проверка совпадения новых паролей
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Пароли не совпадают");
            model.addAttribute("activeTab", "change-password");
            model.addAttribute("passwordDto", dto);
            User user = (User) userService.loadUserByUsername(authentication.getName());
            model.addAttribute("user", user);
            return "profile/main";
        }

        boolean success = userService.changePassword(authentication.getName(), dto);
        if (success) {
            String encodedMessage = URLEncoder.encode("Пароль успешно изменён", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=overview&message=" + encodedMessage;
        } else {
            bindingResult.rejectValue("currentPassword", "error.currentPassword", "Неверный текущий пароль");
            model.addAttribute("activeTab", "change-password");
            model.addAttribute("passwordDto", dto);
            User user = (User) userService.loadUserByUsername(authentication.getName());
            model.addAttribute("user", user);
            return "profile/main";
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