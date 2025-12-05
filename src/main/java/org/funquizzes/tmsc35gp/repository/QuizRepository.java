package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreator(User creator);
    List<Quiz> findByIsPublicTrue();

}
