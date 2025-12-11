package org.funquizzes.tmsc35gp.configuration;

import org.funquizzes.tmsc35gp.entity.Category;
import org.funquizzes.tmsc35gp.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CategoryInitializer {

    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {

        return args -> {
            if (categoryRepository.count() == 0) {
                List<Category> defaultCategories = Arrays.asList(
                        new Category("Программирование", "Вопросы о языках программирования, алгоритмах и технологиях"),
                        new Category("Математика", "Задачи и вопросы по математике"),
                        new Category("История", "Исторические события и личности"),
                        new Category("Наука", "Естественные науки: физика, химия, биология"),
                        new Category("Искусство", "Живопись, музыка, литература, кино"),
                        new Category("Спорт", "Спортивные события, команды и атлеты"),
                        new Category("География", "Страны, города, реки и горы"),
                        new Category("Фильмы и сериалы", "Кинематограф и телевидение"),
                        new Category("Игры", "Видеоигры, настольные игры"),
                        new Category("Еда и кулинария", "Кухни мира, рецепты, продукты"),
                        new Category("Технологии", "Гаджеты, инновации, IT-новости"),
                        new Category("Природа", "Животные, растения, экология"),
                        new Category("Медицина", "Здоровье, болезни, лекарства"),
                        new Category("Экономика", "Финансы, бизнес, рынки"),
                        new Category("Психология", "Поведение человека, эмоции, мышление")
                );

                defaultCategories.get(0).setIcon("bi-code-slash");
                defaultCategories.get(0).setColor("#3b82f6");
                defaultCategories.get(1).setIcon("bi-calculator");
                defaultCategories.get(1).setColor("#ef4444");
                defaultCategories.get(2).setIcon("bi-clock-history");
                defaultCategories.get(2).setColor("#f59e0b");

                categoryRepository.saveAll(defaultCategories);
                System.out.println("Категории успешно инициализированы");
            }
        };
    }
}
