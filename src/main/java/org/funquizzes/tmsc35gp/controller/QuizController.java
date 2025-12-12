package org.funquizzes.tmsc35gp.controller;

import jakarta.validation.Valid;
import org.funquizzes.tmsc35gp.dto.CreateQuestionDto;
import org.funquizzes.tmsc35gp.dto.CreateQuizDto;
import org.funquizzes.tmsc35gp.entity.*;
import org.funquizzes.tmsc35gp.service.CategoryService;
import org.funquizzes.tmsc35gp.service.QuizService;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;


    // страница создание викторины
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        CreateQuizDto dto = new CreateQuizDto();
        List<CreateQuestionDto> questions = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            CreateQuestionDto question = new CreateQuestionDto();
            question.setQuestionIndex(i);
            question.setType(QuestionType.SINGLE_CHOICE);
            question.setPoints(100);
            question.setTimeLimitSeconds(30);

            // 2 варианта ответа по умолчанию (не 4)
            List<String> options = new ArrayList<>();
            options.add("Вариант 1");
            options.add("Вариант 2");
            question.setOptions(options);

            // Пустые изображения
            List<String> optionImages = new ArrayList<>();
            optionImages.add("");
            optionImages.add("");
            question.setOptionImages(optionImages);

            questions.add(question);
        }
        dto.setQuestions(questions);

        // список категорий
        List<Category> categories = categoryService.getAllActiveCategories();

        model.addAttribute("quizDto", dto);
        model.addAttribute("categories", categories);
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("questionTypes", QuestionType.values());

        return "quizzes/create";
    }

    // создание викторины по запросу
    @PostMapping("/create")
    public String createQuiz(@Valid @ModelAttribute("quizDto") CreateQuizDto dto, BindingResult bindingResult,
                             Authentication authentication,
                             Model model) {

        System.out.println("=== ПРОВЕРКА ВОПРОСОВ ===");
        if (dto.getQuestions() != null) {
            for (int i = 0; i < dto.getQuestions().size(); i++) {
                CreateQuestionDto question = dto.getQuestions().get(i);
                System.out.println("Вопрос " + (i + 1) + ":");
                System.out.println("  Тип: " + question.getType());
                System.out.println("  Опции: " + question.getOptions());
                System.out.println("  Кол-во опций: " + (question.getOptions() != null ? question.getOptions().size() : 0));
                System.out.println("  OptionImages: " + question.getOptionImages());
                System.out.println("  Правильные ответы: " + question.getCorrectAnswers());
                System.out.println("  Текст ответ: " + question.getCorrectTextAnswer());

                // Проверка валидации
                System.out.println("  isChoiceAnswerValid(): " + question.isChoiceAnswerValid());
                System.out.println("  isTextAnswerValid(): " + question.isTextAnswerValid());

                // Проверка для TRUE_FALSE
                if (question.getType() == QuestionType.TRUE_FALSE) {
                    System.out.println("  TRUE_FALSE - правильные ответы должны быть 0 или 1: " + question.getCorrectAnswers());
                }
            }
        }

        System.out.println("=== НАЧАЛО СОЗДАНИЯ ВИКТОРИНЫ ===");
        System.out.println("Название: " + dto.getTitle());
        System.out.println("Описание: " + dto.getDescription());
        System.out.println("Публичная: " + dto.isPublic());
        System.out.println("Уровень сложности: " + dto.getDifficultyLevel());
        System.out.println("Категория ID: " + dto.getCategoryId());
        System.out.println("Макс вопросов: " + dto.getMaxQuestions());
        System.out.println("Ограничение времени: " + dto.getTimeLimitMinutes());

        if (dto.getQuestions() != null) {
            System.out.println("Количество вопросов: " + dto.getQuestions().size());
            for (int i = 0; i < dto.getQuestions().size(); i++) {
                CreateQuestionDto question = dto.getQuestions().get(i);
                System.out.println("--- Вопрос " + (i + 1) + " ---");
                System.out.println("Текст: " + question.getText());
                System.out.println("Тип: " + question.getType());
                System.out.println("Баллы: " + question.getPoints());
                System.out.println("Время: " + question.getTimeLimitSeconds());
                System.out.println("Индекс: " + question.getQuestionIndex());

                if (question.getOptions() != null) {
                    System.out.println("Варианты: " + question.getOptions());
                    System.out.println("Кол-во вариантов: " + question.getOptions().size());
                }

                if (question.getCorrectAnswers() != null) {
                    System.out.println("Правильные ответы: " + question.getCorrectAnswers());
                }

                if (question.getCorrectTextAnswer() != null) {
                    System.out.println("Текстовый ответ: " + question.getCorrectTextAnswer());
                }
            }
        } else {
            System.out.println("Вопросы: NULL!");
        }

        if (bindingResult.hasErrors()) {
            System.out.println("=== ОШИБКИ ВАЛИДАЦИИ ===");
            bindingResult.getAllErrors().forEach(error ->
                    System.out.println(error.getDefaultMessage()));

            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("questionTypes", QuestionType.values());

            return "quizzes/create";
        }

        try {
            String username = authentication.getName();
            User creator = (User) userService.loadUserByUsername(username);

            System.out.println("Создатель: " + creator.getUsername());
            Quiz createdQuiz = quizService.createQuiz(dto, creator);
            System.out.println("Викторина создана успешно, ID: " + createdQuiz.getId());

            String encodedMessage = URLEncoder.encode("Викторина успешно создана", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=my-quizzes&message=" + encodedMessage;

        } catch (Exception e) {
            System.out.println("=== ОШИБКА ПРИ СОЗДАНИИ ВИКТОРИНЫ ===");
            e.printStackTrace();

            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("error", "Ошибка: " + e.getMessage());

            return "quizzes/create";
        }
    }

    // delete quiz
    @GetMapping("/delete/{id}")
    public String deleteQuiz(@PathVariable Long id, Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        try {
            Quiz quiz = quizService.findById(id);
            User currentUser = (User) userService.loadUserByUsername(authentication.getName());

            if (quiz != null && quiz.getCreator().getId().equals(currentUser.getId())) {
                quizService.deleteQuiz(id);
                String encodedMessage = URLEncoder.encode("Викторина успешно удалена", StandardCharsets.UTF_8);
                return "redirect:/users/profile/main?tab=my-quizzes&message=" + encodedMessage;
            } else {
                String encodedMessage = URLEncoder.encode("Вы не можете удалить эту викторину", StandardCharsets.UTF_8);
                return "redirect:/users/profile/main?tab=my-quizzes&message=" + encodedMessage;
            }
        } catch (Exception e) {
            String encodedMessage = URLEncoder.encode("Ошибка при удалении викторины", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=my-quizzes&message=" + encodedMessage;
        }
    }

    @GetMapping("/my")
    public String myQuizzes(Model model, Authentication authentication) {

        User user = (User) userService.loadUserByUsername(authentication.getName());
        List<Quiz> quizzes = quizService.findByCreator(user);
        model.addAttribute("quizzes", quizzes);

        return "quizzes/my";
    }

    @PostMapping("/upload-image")
    @ResponseBody
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imagePath = quizService.saveImage(file);
            return "{\"success\": true, \"path\": \"" + imagePath + "\"}";
        } catch (Exception e) {
            return "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/add-question")
    public String addQuestionFragment(@RequestParam("index") int index, Model model) {
        CreateQuestionDto question = new CreateQuestionDto();
        question.setQuestionIndex(index + 1);
        question.setType(QuestionType.SINGLE_CHOICE); // По умолчанию один правильный ответ
        question.setPoints(100);
        question.setTimeLimitSeconds(30);

        // 2 варианта ответа по умолчанию
        List<String> options = new ArrayList<>();
        options.add("Вариант 1");
        options.add("Вариант 2");
        question.setOptions(options);

        // Пустые изображения для вариантов
        List<String> optionImages = new ArrayList<>();
        optionImages.add("");
        optionImages.add("");
        question.setOptionImages(optionImages);

        model.addAttribute("question", question);
        model.addAttribute("questionIndex", index);
        model.addAttribute("questionTypes", QuestionType.values());

        return "fragments/question-form :: questionForm";
    }
}