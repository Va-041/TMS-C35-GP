package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByName();
    Category findByName(String name);
    boolean existsByName(String name);

    // метод для поиска категорий, в которых есть публичные викторины
    @Query("SELECT DISTINCT c FROM Category c JOIN c.quizzes q WHERE c.active = true AND q.isPublic = true ORDER BY c.name")
    List<Category> findActiveCategoriesWithPublicQuizzes();
}