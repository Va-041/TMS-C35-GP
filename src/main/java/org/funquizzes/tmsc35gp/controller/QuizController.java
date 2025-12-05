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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

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

        return "redirect:/quizzes/my";
    }

    @GetMapping("/my")
    public String myQuizzes(Model model, Authentication authentication) {

        User user = (User) userService.loadUserByUsername(authentication.getName());
        List<Quiz> quizzes = quizService.findByCreator(user);
        model.addAttribute("quizzes", quizzes);

        return "myQuizzes";
    }
}
