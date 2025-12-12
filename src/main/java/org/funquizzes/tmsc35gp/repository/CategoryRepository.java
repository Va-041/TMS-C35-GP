package org.funquizzes.tmsc35gp.repository;

import org.funquizzes.tmsc35gp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByName();
    Category findByName(String name);
    boolean existsByName(String name);
}
