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
                        new Category("Кино", "Фильмы, сериалы и телевидение"),
                        new Category("Игры", "Видеоигры, настольные игры"),
                        new Category("Еда и кулинария", "Кухни мира, рецепты, продукты"),
                        new Category("Технологии", "Гаджеты, инновации, IT-новости"),
                        new Category("Природа", "Животные, растения, экология"),
                        new Category("Здоровье", "Медицина и здоровый образ жизни"),
                        new Category("Экономика", "Финансы, бизнес, рынки"),
                        new Category("Психология", "Поведение человека, эмоции, мышление")
                );

                // Устанавливаем иконки для всех категорий
                String[] icons = {
                        "bi-code-slash",      // Программирование
                        "bi-calculator",      // Математика
                        "bi-clock-history",   // История
                        "bi-cpu",             // Наука
                        "bi-palette",         // Искусство
                        "bi-trophy",          // Спорт
                        "bi-globe",           // География
                        "bi-film",            // Фильмы и сериалы
                        "bi-controller",      // Игры
                        "bi-egg-fried",       // Еда и кулинария
                        "bi-laptop",          // Технологии
                        "bi-tree",            // Природа
                        "bi-heart-pulse",     // Медицина
                        "bi-cash-coin",       // Экономика
                        "bi-brain"            // Психология
                };

                // Устанавливаем цвета для всех категорий
                String[] colors = {
                        "#3b82f6", // Синий - Программирование
                        "#ef4444", // Красный - Математика
                        "#f59e0b", // Оранжевый - История
                        "#10b981", // Зеленый - Наука
                        "#8b5cf6", // Фиолетовый - Искусство
                        "#ec4899", // Розовый - Спорт
                        "#0ea5e9", // Голубой - География
                        "#84cc16", // Лаймовый - Фильмы и сериалы
                        "#f97316", // Оранжевый темный - Игры
                        "#6366f1", // Индиго - Еда и кулинария
                        "#14b8a6", // Бирюзовый - Технологии
                        "#f43f5e", // Фуксия - Природа
                        "#06b6d4", // Циан - Медицина
                        "#22c55e", // Зеленый яркий - Экономика
                        "#a855f7"  // Пурпурный - Психология
                };

                for (int i = 0; i < defaultCategories.size(); i++) {
                    if (i < icons.length) {
                        defaultCategories.get(i).setIcon(icons[i]);
                    }
                    if (i < colors.length) {
                        defaultCategories.get(i).setColor(colors[i]);
                    }
                }

                categoryRepository.saveAll(defaultCategories);
                System.out.println("Категории успешно инициализированы");
            }
        };
    }
}