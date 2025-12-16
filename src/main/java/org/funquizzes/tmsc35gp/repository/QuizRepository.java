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
import java.util.Optional;

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

    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND " +
            "(:search IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(q.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:categoryIds IS NULL OR q.category.id IN :categoryIds) AND " +
            "(:difficultyLevels IS NULL OR q.difficultyLevel IN :difficultyLevels)")
    Page<Quiz> findPublicQuizzesWithFilters(
            @Param("search") String search,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("difficultyLevels") List<DifficultyLevel> difficultyLevels,
            Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND " +
            "(:categories IS NULL OR q.category.id IN :categories)")
    Page<Quiz> findPublicQuizzesByCategories(@Param("categories") List<Long> categoryIds, Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.isPublic = true AND " +
            "(:categories IS NULL OR q.category.id IN :categories) AND " +
            "(:difficulties IS NULL OR q.difficultyLevel IN :difficulties)")
    Page<Quiz> findPublicQuizzesWithFilters(
            @Param("categories") List<Long> categoryIds,
            @Param("difficulties") List<DifficultyLevel> difficulties,
            Pageable pageable);

    // методы для проверки существования викторин
    @Query("SELECT COUNT(q) > 0 FROM Quiz q WHERE q.category.id = :categoryId AND q.isPublic = true")
    boolean existsByCategoryIdAndPublicTrue(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.category.id = :categoryId AND q.isPublic = true")
    long countByCategoryIdAndPublicTrue(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(q) > 0 FROM Quiz q WHERE q.category.id IN :categoryIds AND q.isPublic = true")
    boolean existsByCategoryIdsAndPublicTrue(@Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT DISTINCT q FROM Quiz q " +
            "LEFT JOIN FETCH q.questions " +
            "LEFT JOIN FETCH q.creator " +
            "LEFT JOIN FETCH q.category " +
            "WHERE q.id = :id AND q.isPublic = true")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
}