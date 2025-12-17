package org.funquizzes.tmsc35gp.controller;

import jakarta.validation.Valid;
import org.funquizzes.tmsc35gp.dto.CreateQuizRatingAfterPlayDto;
import org.funquizzes.tmsc35gp.dto.CreateQuizRatingDto;
import org.funquizzes.tmsc35gp.dto.UpdateQuizRatingDto;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.QuizRating;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.service.QuizRatingService;
import org.funquizzes.tmsc35gp.service.QuizService;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/ratings")
public class QuizRatingController {

    @Autowired
    private QuizRatingService ratingService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    @Autowired
    private PlayQuizController playQuizController;



    // Оценить викторину
    @PostMapping("/rate")
    public String rateQuiz(@Valid @ModelAttribute CreateQuizRatingDto ratingDto,
                           BindingResult bindingResult,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка валидации: " + bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/quizzes/details/" + ratingDto.getQuizId();
        }

        try {
            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);
            Quiz quiz = quizService.findById(ratingDto.getQuizId());

            if (quiz == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Викторина не найдена");
                return "redirect:/quizzes";
            }

            if (quiz.getCreator().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Вы не можете оценить свою викторину");
                return "redirect:/quizzes/details/" + ratingDto.getQuizId();
            }

            // Оцениваем викторину
            ratingService.rateQuiz(quiz, user, ratingDto.getRating(), ratingDto.getComment());

            redirectAttributes.addFlashAttribute("successMessage", "Викторина успешно оценена!");
            return "redirect:/quizzes/details/" + ratingDto.getQuizId() + "?tab=ratings";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при оценке викторины: " + e.getMessage());
            return "redirect:/quizzes/details/" + ratingDto.getQuizId();
        }
    }

    @PostMapping("/rate-after-play")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rateQuizAfterPlay(
            @RequestBody @Valid CreateQuizRatingAfterPlayDto ratingDto,
            BindingResult bindingResult,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Ошибка валидации: " + bindingResult.getFieldError().getDefaultMessage());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);
            Quiz quiz = quizService.findById(ratingDto.getQuizId());

            if (quiz == null) {
                response.put("success", false);
                response.put("message", "Викторина не найдена");
                return ResponseEntity.badRequest().body(response);
            }

            if (quiz.getCreator().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Вы не можете оценить свою викторину");
                return ResponseEntity.badRequest().body(response);
            }

            // Проверяем, оценивал ли пользователь викторину ранее
            boolean hasRated = ratingService.hasUserRatedQuiz(quiz, user);
            if (hasRated) {
                response.put("success", false);
                response.put("message", "Вы уже оценили эту викторину");
                return ResponseEntity.badRequest().body(response);
            }

            // Оцениваем викторину
            QuizRating savedRating = ratingService.rateQuiz(
                    quiz, user, ratingDto.getRating(), ratingDto.getComment());

            // Очищаем игровую сессию
            playQuizController.cleanupGameSession(ratingDto.getSessionId());

            response.put("success", true);
            response.put("message", "Викторина успешно оценена!");
            response.put("ratingId", savedRating.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при оценке викторины: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Обновить оценку
    @PostMapping("/update/{id}")
    public String updateRating(@PathVariable Long id,
                               @Valid @ModelAttribute UpdateQuizRatingDto ratingDto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               @RequestParam(name = "returnTo", defaultValue = "quiz") String returnTo,
                               @RequestParam(name = "quizId", required = false) Long quizId) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Ошибка валидации: " + bindingResult.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);

            // Возвращаем туда, откуда пришли
            if ("profile".equals(returnTo)) {
                return "redirect:/users/profile/main?tab=activity";
            } else {
                return "redirect:/quizzes/details/" + quizId + "?tab=ratings";
            }
        }

        try {
            QuizRating rating = ratingService.getRatingById(id);
            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);

            if (!rating.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Вы не можете редактировать эту оценку");

                if ("profile".equals(returnTo)) {
                    return "redirect:/users/profile/main?tab=activity";
                } else {
                    return "redirect:/quizzes/details/" + rating.getQuiz().getId() + "?tab=ratings";
                }
            }

            // Сохраняем ID викторины перед обновлением
            Long currentQuizId = rating.getQuiz().getId();
            ratingService.updateRating(id, ratingDto);

            redirectAttributes.addFlashAttribute("successMessage", "Оценка обновлена!");

            // куда вернуться
            if ("profile".equals(returnTo)) {
                return "redirect:/users/profile/main?tab=activity";
            } else {
                return "redirect:/quizzes/details/" + currentQuizId + "?tab=ratings";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при обновлении оценки: " + e.getMessage());

            // Возвращаем на страницу викторины по умолчанию
            if ("profile".equals(returnTo)) {
                return "redirect:/users/profile/main?tab=activity";
            } else {
                return quizId != null ?
                        "redirect:/quizzes/details/" + quizId + "?tab=ratings" :
                        "redirect:/quizzes";
            }
        }
    }
    // Удалить оценку
    @GetMapping("/delete/{id}")
    public String deleteRating(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        try {
            QuizRating rating = ratingService.getRatingById(id);
            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);

            if (!rating.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Вы не можете удалить эту оценку");
                return "redirect:/users/profile/main?tab=activity";
            }

            Long quizId = rating.getQuiz().getId();
            ratingService.deleteRating(id);

            redirectAttributes.addFlashAttribute("successMessage", "Оценка удалена!");
            return "redirect:/quizzes/details/" + quizId + "?tab=ratings";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении оценки: " + e.getMessage());
            return "redirect:/users/profile/main?tab=activity";
        }
    }

    // Проверить, оценил ли пользователь викторину
    @GetMapping("/check/{quizId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkUserRating(@PathVariable Long quizId,
                                                               Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Если пользователь не аутентифицирован, возвращаем false
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("hasRated", false);
                return ResponseEntity.ok(response);
            }

            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);
            Quiz quiz = quizService.findById(quizId);

            if (quiz == null) {
                response.put("hasRated", false);
                return ResponseEntity.ok(response);
            }

            boolean hasRated = ratingService.hasUserRatedQuiz(quiz, user);
            response.put("hasRated", hasRated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // В случае ошибки - пользователь не оценивал
            response.put("hasRated", false);
            return ResponseEntity.ok(response);
        }
    }

    // Получить распределение оценок
    @GetMapping("/distribution/{quizId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRatingDistribution(@PathVariable Long quizId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Quiz quiz = quizService.findById(quizId);
            Map<String, Integer> distribution = ratingService.getRatingDistribution(quiz);

            int totalRatings = distribution.values().stream().mapToInt(Integer::intValue).sum();
            response.put("distribution", distribution);
            response.put("totalRatings", totalRatings);
            response.put("averageRating", quiz.getAverageRating());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Получить оценки викторины (для страницы деталей)
    @GetMapping("/quiz/{quizId}")
    public String getQuizRatings(@PathVariable Long quizId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model,
                                 Authentication authentication) {

        try {
            Quiz quiz = quizService.findById(quizId);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<QuizRating> ratingsPage = ratingService.getQuizRatings(quiz, pageable);

            model.addAttribute("quiz", quiz);
            model.addAttribute("ratings", ratingsPage.getContent());
            model.addAttribute("totalPages", ratingsPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("averageRating", quiz.getAverageRating());
            model.addAttribute("ratingCount", quiz.getRatingCounts());

            // Добавляем currentUser для проверки прав на редактирование
            if (authentication != null && authentication.isAuthenticated()) {
                User currentUser = (User) userService.loadUserByUsername(authentication.getName());
                model.addAttribute("currentUser", currentUser);
            }

            return "fragments/rating-list :: ratingsList";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке оценок: " + e.getMessage());
            return "fragments/rating-list :: ratingsList";
        }
    }

    // Получить оценки пользователя
    @GetMapping("/user/{userId}")
    public String getUserRatings(@PathVariable Long userId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model,
                                 Authentication authentication) {

        try {
            User currentUser = (User) userService.loadUserByUsername(authentication.getName());
            User targetUser = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // имеет ли текущий пользователь доступ
            boolean canView = targetUser.isPublicProfile() ||
                    targetUser.getId().equals(currentUser.getId()) ||
                    targetUser.getFriends().contains(currentUser);

            if (!canView) {
                model.addAttribute("error", "У вас нет доступа к оценкам этого пользователя");
                return "fragments/ratings-list :: ratingsList";
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<QuizRating> ratingsPage = ratingService.getUserRatings(targetUser, pageable);

            model.addAttribute("user", targetUser);
            model.addAttribute("ratings", ratingsPage.getContent());
            model.addAttribute("totalPages", ratingsPage.getTotalPages());
            model.addAttribute("currentPage", page);

            return "fragments/user-ratings-list :: userRatingsList";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке оценок пользователя: " + e.getMessage());
            return "fragments/ratings-list :: ratingsList";
        }
    }

    // Получить данные конкретной оценки по ID
    @GetMapping("/get/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRatingById(@PathVariable Long id,
                                                             Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "Требуется авторизация");
                return ResponseEntity.status(401).body(response);
            }

            String username = authentication.getName();
            User user = (User) userService.loadUserByUsername(username);

            QuizRating rating = ratingService.getRatingByIdAndUser(id, user);

            if (rating == null) {
                response.put("success", false);
                response.put("message", "Оценка не найдена или у вас нет доступа");
                return ResponseEntity.status(404).body(response);
            }

            response.put("success", true);
            response.put("ratingId", rating.getId());
            response.put("rating", rating.getRating());
            response.put("comment", rating.getComment());
            response.put("quizId", rating.getQuiz().getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка при загрузке оценки: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}