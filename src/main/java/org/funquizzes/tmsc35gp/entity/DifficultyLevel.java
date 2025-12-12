package org.funquizzes.tmsc35gp.entity;

import lombok.Getter;

@Getter
public enum DifficultyLevel {
    EASY("Легкий", "#10b981"),
    MEDIUM("Средний", "#f59e0b"),
    HARD("Сложный", "#ef4444");

    private final String displayName;
    private final String color;

    DifficultyLevel(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

}
