package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.QuizRating;
import org.funquizzes.tmsc35gp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRatingRepository extends JpaRepository<QuizRating, Long> {

    Optional<QuizRating> findByQuizAndUser(Quiz quiz, User user);
    boolean existsByQuizAndUser (Quiz quiz, User user);
    long countByQuiz(Quiz quiz);

    @Query("SELECT r FROM QuizRating r WHERE r.quiz = :quiz")
    List<QuizRating> findByQuiz(Quiz quiz);
}
