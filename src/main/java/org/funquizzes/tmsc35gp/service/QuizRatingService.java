package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.dto.UpdateQuizRatingDto;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.QuizRating;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.repository.QuizRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizRatingService {

    @Autowired
    private QuizRatingRepository quizRatingRepository;

    @Autowired
    private QuizService quizService;

    @Transactional
    public QuizRating rateQuiz(Quiz quiz, User user, Double rating, String comment) {
        // проверяем, оценивал ли пользователь эту викторину ранее
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

        quizRating.setRating(rating);
        quizRating.setComment(comment);

        QuizRating savedRating = quizRatingRepository.save(quizRating);

        // обновляем рейтинг квиза
        if (isNewRating) {
            quizService.updateQuizRating(quiz.getId(), rating);
        } else {
            // пересчитываем все оценки викторины
            recalculateQuizRating(quiz);
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
        return quizRatingRepository.findAverageRatingByQuiz(quiz)
                .orElse(0.0);
    }

    public long getRatingCount(Quiz quiz) {
        return quizRatingRepository.countByQuiz(quiz);
    }

    public QuizRating getRatingById(Long ratingId) {
        return quizRatingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Рейтинг не найден"));
    }

    @Transactional
    public QuizRating updateRating(Long ratingId, UpdateQuizRatingDto dto) {
        QuizRating rating = getRatingById(ratingId);
        rating.setRating(dto.getRating());
        rating.setComment(dto.getComment());

        QuizRating updatedRating = quizRatingRepository.save(rating);

        // Пересчитываем рейтинг викторины
        recalculateQuizRating(rating.getQuiz());
        quizService.saveQuiz(rating.getQuiz());

        return updatedRating;
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        QuizRating rating = getRatingById(ratingId);
        Quiz quiz = rating.getQuiz();
        quizRatingRepository.delete(rating);

        // Пересчитываем рейтинг викторины после удаления
        recalculateQuizRating(quiz);
        quizService.saveQuiz(quiz);
    }

    public Page<QuizRating> getQuizRatings(Quiz quiz, Pageable pageable) {
        return quizRatingRepository.findAllByQuizOrderByCreatedAtDesc(quiz, pageable);
    }

    public Page<QuizRating> getUserRatings(User user, Pageable pageable) {
        return quizRatingRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);
    }

    public Double calculateAverageRating(Long quizId) {
        Quiz quiz = quizService.findById(quizId);
        if (quiz == null) {
            return 0.0;
        }
        return getAverageRating(quiz);
    }

    public Map<String, Integer> getRatingDistribution(Quiz quiz) {
        List<QuizRating> ratings = quizRatingRepository.findByQuiz(quiz);

        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(String.valueOf(i), 0);
        }

        for (QuizRating rating : ratings) {
            int star = (int) Math.round(rating.getRating());
            distribution.put(String.valueOf(star), distribution.getOrDefault(String.valueOf(star), 0) + 1);
        }

        return distribution;
    }

    @Transactional
    public boolean canUserEditRating(Long ratingId, String username) {
        QuizRating rating = getRatingById(ratingId);
        return rating.getUser().getUsername().equals(username);
    }

    @Transactional
    public void deleteAllRatingsForQuiz(Quiz quiz) {
        quizRatingRepository.deleteByQuiz(quiz);
        quiz.setAverageRating(0.0);
        quiz.setRatingCounts(0);
        quizService.saveQuiz(quiz);
    }

    public List<QuizRating> getRecentRatings(Quiz quiz, int limit) {
        List<QuizRating> ratings = quizRatingRepository.findRecentRatingsByQuiz(quiz);
        return ratings.size() > limit ? ratings.subList(0, limit) : ratings;
    }

    public boolean hasUserCompletedQuiz(Long quizId, String username) {
        try {
            Quiz quiz = quizService.findById(quizId);
            if (quiz == null) return false;

            // нужно реализовать логику проверки,
            // что пользователь завершил викторину
            // Можно использовать статистику пользователя или отдельную таблицу завершений

            // Временно - возвращаем true
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // проверка возможности оценки
    public boolean canUserRateQuiz(Quiz quiz, User user) {
        // Пользователь не может оценить свою викторину
        if (quiz.getCreator().getId().equals(user.getId())) {
            return false;
        }

        // оценивал ли пользователь викторину ранее
        boolean hasRated = hasUserRatedQuiz(quiz, user);
        if (hasRated) {
            return false;
        }
        // завершил ли пользователь викторину
        return true;
    }

    public QuizRating getRatingByIdAndUser(Long ratingId, User user) {
        return quizRatingRepository.findByIdAndUser(ratingId, user)
                .orElseThrow(() -> new RuntimeException("Оценка не найдена или у вас нет доступа"));
    }
}