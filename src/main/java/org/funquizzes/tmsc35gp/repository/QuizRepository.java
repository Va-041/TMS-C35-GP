package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Category;
import org.funquizzes.tmsc35gp.entity.DifficultyLevel;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.funquizzes.tmsc35gp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreator(User creator);
    List<Quiz> findByIsPublicTrue();

    Page<Quiz> findByIsPublicTrue(Pageable pageable);
    Page<Quiz> findByCategoryAndIsPublicTrue(Category category, Pageable pageable);
    Page<Quiz> findByDifficultyLevelAndIsPublicTrue(DifficultyLevel difficultyLevel, Pageable pageable);

    // поиск викторин со статусом Публичная
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND " +
            "(:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Quiz> searchPublicQuizzes(@Param("search") String search, Pageable pageable);

    // поиск популярных викторин (по количеству запусков по убыванию)
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true ORDER BY q.playsCount DESC")
    Page<Quiz> findPopularQuizzes(Pageable pageable);

    // поиск викторин по рейтингу
    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true ORDER BY q.averageRating DESC")
    Page<Quiz> findTopRatedQuizzes(Pageable pageable);

}
