package org.funquizzes.tmsc35gp.entity;

import lombok.Getter;

@Getter
public enum QuestionType {
    TRUE_FALSE("Правда/Ложь"),
    SINGLE_CHOICE("Один правильный ответ"),
    MULTIPLE_CHOICE("Несколько правильных ответов"),
    TEXT_INPUT("Текстовый ответ");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

}
