package org.funquizzes.tmsc35gp.controller;

import lombok.extern.slf4j.Slf4j;
import org.funquizzes.tmsc35gp.dto.StatsDTO;
import org.funquizzes.tmsc35gp.entity.Category;
import org.funquizzes.tmsc35gp.service.AnalyticService;
import org.funquizzes.tmsc35gp.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/")
public class IndexPageController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AnalyticService analyticService;

    @GetMapping
    public String index(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            log.info("User logged in: {}, Authorities: {}",
                    auth.getName(), auth.getAuthorities());
        } else {
            log.info("No user logged in");
        }

        StatsDTO stats = analyticService.getHomePageStats();

        // Получаем все активные категории
        List<Category> categories = categoryService.getAllActiveCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("stats", stats);
        model.addAttribute("categories", categoryService.getAllActiveCategories());

        // Устанавливаем цвета и иконки по умолчанию для категорий без них
        for (Category category : categories) {
            if (category.getColor() == null || category.getColor().isEmpty()) {
                // Генерируем случайный цвет на основе имени категории
                String[] defaultColors = {
                        "#3b82f6", "#ef4444", "#f59e0b", "#10b981",
                        "#8b5cf6", "#ec4899", "#0ea5e9", "#84cc16",
                        "#f97316", "#6366f1", "#14b8a6", "#f43f5e",
                        "#06b6d4", "#22c55e", "#a855f7"
                };
                int colorIndex = Math.abs(category.getName().hashCode()) % defaultColors.length;
                category.setColor(defaultColors[colorIndex]);
            }

            // Устанавливаем иконки по умолчанию
            if (category.getIcon() == null || category.getIcon().isEmpty()) {
                String icon = getIconForCategory(category.getName());
                category.setIcon(icon);
            }
        }

        return "index";
    }

    private String getIconForCategory(String categoryName) {
        String lowerName = categoryName.toLowerCase();

        if (lowerName.contains("программ") || lowerName.contains("код")) {
            return "bi-code-slash";
        } else if (lowerName.contains("матем")) {
            return "bi-calculator";
        } else if (lowerName.contains("история")) {
            return "bi-clock-history";
        } else if (lowerName.contains("наука") || lowerName.contains("физика") || lowerName.contains("химия") || lowerName.contains("биология")) {
            return "bi-cpu";
        } else if (lowerName.contains("искусств") || lowerName.contains("живопись") || lowerName.contains("музыка") || lowerName.contains("литература")) {
            return "bi-palette";
        } else if (lowerName.contains("спорт")) {
            return "bi-trophy";
        } else if (lowerName.contains("географ")) {
            return "bi-globe";
        } else if (lowerName.contains("фильм") || lowerName.contains("кино") || lowerName.contains("сериал")) {
            return "bi-film";
        } else if (lowerName.contains("игр")) {
            return "bi-controller";
        } else if (lowerName.contains("еда") || lowerName.contains("кулин")) {
            return "bi-egg-fried";
        } else if (lowerName.contains("технолог") || lowerName.contains("гаджет")) {
            return "bi-laptop";
        } else if (lowerName.contains("природ") || lowerName.contains("животн") || lowerName.contains("растен")) {
            return "bi-tree";
        } else if (lowerName.contains("медицина") || lowerName.contains("здоров") || lowerName.contains("болезн")) {
            return "bi-heart-pulse";
        } else if (lowerName.contains("экономика") || lowerName.contains("финанс") || lowerName.contains("бизнес")) {
            return "bi-cash-coin";
        } else if (lowerName.contains("психология") || lowerName.contains("поведен")) {
            return "bi-brain";
        } else {
            return "bi-tag";
        }
    }
}