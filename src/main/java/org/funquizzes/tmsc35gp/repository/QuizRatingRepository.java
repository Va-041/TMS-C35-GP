package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.QuizRating;
import org.funquizzes.tmsc35gp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRatingRepository extends JpaRepository<QuizRating, Long>, JpaSpecificationExecutor<QuizRating> {

    Optional<QuizRating> findByQuizAndUser(Quiz quiz, User user);

    boolean existsByQuizAndUser(Quiz quiz, User user);

    long countByQuiz(Quiz quiz);

    List<QuizRating> findByQuiz(Quiz quiz);

    List<QuizRating> findByUser(User user);

    List<QuizRating> findByQuizOrderByCreatedAtDesc(Quiz quiz);

    @Query("SELECT AVG(r.rating) FROM QuizRating r WHERE r.quiz = :quiz")
    Optional<Double> findAverageRatingByQuiz(@Param("quiz") Quiz quiz);

    @Query("SELECT r FROM QuizRating r WHERE r.quiz = :quiz ORDER BY r.createdAt DESC")
    List<QuizRating> findRecentRatingsByQuiz(@Param("quiz") Quiz quiz);

    Page<QuizRating> findAllByQuizOrderByCreatedAtDesc(Quiz quiz, Pageable pageable);

    Page<QuizRating> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<QuizRating> findAllByQuiz(Quiz quiz, Pageable pageable);

    Page<QuizRating> findAllByUser(User user, Pageable pageable);

    // Удалить все оценки для викторины
    void deleteByQuiz(Quiz quiz);

    // Удалить все оценки пользователя
    void deleteByUser(User user);

    // есть ли оценки у викторины
    boolean existsByQuiz(Quiz quiz);

    Optional<QuizRating> findByIdAndUser(Long id, User user);
}