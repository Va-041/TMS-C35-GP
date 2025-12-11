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

        for(int i = 1; i <= 10; i++) {
            CreateQuestionDto question = new CreateQuestionDto();
            question.setQuestionIndex(i);
            question.setType(QuestionType.SINGLE_CHOICE);
            question.setPoints(100);
            question.setTimeLimitSeconds(30);

            // варианты ответов по умолчанию: 4
            List<String> options = new ArrayList<>();
            options.add("Вариант 1");
            options.add("Вариант 2");
            options.add("Вариант 3");
            options.add("Вариант 4");
            question.setOptions(options);

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

        System.out.println("Начало создания викторины");
        System.out.println("Количество вопросов: " + (dto.getQuestions() != null ? dto.getQuestions().size() : 0));

        if (bindingResult.hasErrors()) {
            System.out.println("Ошибки валидации: " + bindingResult.getAllErrors());

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
            quizService.createQuiz(dto, creator);
            System.out.println("Викторина создана успешно");

            String encodedMessage = URLEncoder.encode("Викторина успешно создана", StandardCharsets.UTF_8);
            return "redirect:/users/profile/main?tab=my-quizzes&message=" + encodedMessage;

        } catch (Exception e) {
            System.out.println("Ошибка при создании викторины: " + e.getMessage());
            e.printStackTrace();

            List<Category> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("difficultyLevels", DifficultyLevel.values());
            model.addAttribute("questionTypes", QuestionType.values());
            model.addAttribute("error", e.getMessage());

            return "redirect:/quizzes/create";
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
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(100);
        question.setTimeLimitSeconds(30);

        // 4 варианта ответа по умолчанию
        List<String> options = new ArrayList<>();
        options.add("Вариант 1");
        options.add("Вариант 2");
        options.add("Вариант 3");
        options.add("Вариант 4");
        question.setOptions(options);

        model.addAttribute("question", question);
        model.addAttribute("questionIndex", index);
        model.addAttribute("questionTypes", QuestionType.values());

        return "fragments/question-form :: questionForm";
    }
}