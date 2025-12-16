package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN FETCH q.options " +
            "WHERE q.quiz.id = :quizId " +
            "ORDER BY q.questionIndex")
    List<Question> findByQuizIdWithOptions(@Param("quizId") Long quizId);
}