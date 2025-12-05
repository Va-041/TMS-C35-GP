package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.dto.CreateQuizDto;
import org.funquizzes.tmsc35gp.entity.Question;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    public void createQuiz(CreateQuizDto dto, User creator) {

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setIsPublic(dto.isPublic());
        quiz.setCreator(creator);

        List<Question> questions = dto.getQuestion().stream()
                .map(qDto -> {
                    Question question = new Question();
                    question.setText(qDto.getText());
                    question.setFirstOption(qDto.getFirstOption());
                    question.setSecondOption(qDto.getSecondOption());
                    question.setThirdOption(qDto.getThirdOption());
                    question.setFourthOption(qDto.getFourthOption());

                    question.setCorrectAnswerIndex(qDto.getCorrectAnswerIndex());
                    question.setTimeLimitSeconds(qDto.getTimeLimitSeconds());

                    question.setQuiz(quiz);

                    return question;
                })
                .collect(Collectors.toList());

        quiz.setQuestions(questions);
        quizRepository.save(quiz);
    }

    public List<Quiz> findByCreator(User creator) {
        return quizRepository.findByCreator(creator);
    }
}
