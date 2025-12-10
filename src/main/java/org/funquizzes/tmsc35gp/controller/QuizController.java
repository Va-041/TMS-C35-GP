package org.funquizzes.tmsc35gp.controller;

import org.funquizzes.tmsc35gp.dto.CreateQuestionDto;
import org.funquizzes.tmsc35gp.dto.CreateQuizDto;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.service.QuizService;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    // create quiz
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        CreateQuizDto dto = new CreateQuizDto();
        List<CreateQuestionDto> questions = new ArrayList<>();
        for(int i = 1; i <= 3; i++) {
            questions.add(new CreateQuestionDto());
        }
        dto.setQuestion(questions);

        model.addAttribute("quizDto", dto);
        return "createQuiz";
    }

    @PostMapping("/create")
    public String createQuiz(@ModelAttribute CreateQuizDto dto, Authentication authentication) {

        String username = authentication.getName();
        User creator = (User) userService.loadUserByUsername(username);

        quizService.createQuiz(dto, creator);

        String encodedMessage = URLEncoder.encode("Викторина успешно создана", StandardCharsets.UTF_8);

        return "redirect:/users/profile/main?tab=my-quizzes&message=" + encodedMessage;
    }

    // delete quiz
    @GetMapping("/delete/{id}")
    public String deleteQuiz(@PathVariable Long id, Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        try {
            Quiz quiz = quizService.findById(id);
            User currentUser = (User) userService.loadUserByUsername(authentication.getName());

            if(quiz != null && quiz.getCreator().getId().equals(currentUser.getId())) {
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

        return "myQuizzes";
    }
}
