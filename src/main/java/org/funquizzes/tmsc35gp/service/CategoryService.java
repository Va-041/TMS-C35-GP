package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.entity.Category;
import org.funquizzes.tmsc35gp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByName();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Категория с таким названием уже существует");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        if (!category.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
            throw new IllegalArgumentException("Категория с таким названием уже существует");
        }

        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        category.setIcon(updatedCategory.getIcon());
        category.setColor(updatedCategory.getColor());
        category.setActive(updatedCategory.isActive());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        if (!category.getQuizzes().isEmpty()) {
            throw new IllegalStateException("Нельзя удалить категорию, в которой есть викторины");
        }

        categoryRepository.delete(category);
    }
}
