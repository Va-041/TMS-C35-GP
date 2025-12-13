package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.QuizRating;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.repository.QuizRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuizRatingService {

    @Autowired
    private QuizRatingRepository quizRatingRepository;

    @Autowired
    private QuizService quizService;

    @Transactional
    public QuizRating rateQuiz(Quiz quiz, User user, Double rating, String comment) {

        // оценивал ли пользователь эту викторину ранее
        Optional<QuizRating> existingRating = quizRatingRepository.findByQuizAndUser(quiz, user);

        QuizRating quizRating;
        boolean isNewRating = false;

        if (existingRating.isPresent()) {
            quizRating = existingRating.get();
        } else {
            quizRating = new QuizRating();
            quizRating.setQuiz(quiz);
            quizRating.setUser(user);
            isNewRating = true;
        }

        Double oldRating = quizRating.getRating();
        quizRating.setRating(rating);
        quizRating.setComment(comment);

        QuizRating savedRating = quizRatingRepository.save(quizRating);

        // обновляем рейтинг квиза
        if (isNewRating) {
            quizService.updateQuizRating(quiz.getId(), rating);
        } else  {
            // пересчитываем все оценки викторины
            recalculateQuizRating(quiz);
            // сохранить quiz с обновленным рейтингом
            quizService.saveQuiz(quiz);
        }

        return savedRating;
    }

    @Transactional
    public void recalculateQuizRating(Quiz quiz) {
        List<QuizRating> ratings = quizRatingRepository.findByQuiz(quiz);
        if (!ratings.isEmpty()) {
            double average = ratings.stream()
                    .mapToDouble(QuizRating::getRating)
                    .average()
                    .orElse(0.0);

            quiz.setAverageRating(Math.round(average * 10.0) / 10.0);
            quiz.setRatingCounts(ratings.size());
        } else {
            quiz.setAverageRating(0.0);
            quiz.setRatingCounts(0);
        }
    }

    public boolean hasUserRatedQuiz(Quiz quiz, User user) {
        return quizRatingRepository.existsByQuizAndUser(quiz, user);
    }

    public Optional<QuizRating> getUserRating(Quiz quiz, User user) {
        return quizRatingRepository.findByQuizAndUser(quiz, user);
    }

    public double getAverageRating(Quiz quiz) {
        long sum = 0;
        long count = 0;
        for (QuizRating quizRating : quizRatingRepository.findByQuiz(quiz)) {
            Double rating = quizRating.getRating();
            sum += rating;
            count++;
        }
        return count > 0 ? (double) sum / count : 0.0;
    }

    public long getRatingCount(Quiz quiz) {
        return quizRatingRepository.countByQuiz(quiz);
    }

}
