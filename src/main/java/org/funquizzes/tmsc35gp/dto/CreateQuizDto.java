package org.funquizzes.tmsc35gp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.funquizzes.tmsc35gp.entity.DifficultyLevel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizDto {

    @NotBlank(message = "Название обязательно")
    @Size(min = 3, max = 100, message = "Название должно быть от 3 до 100 символов")
    private String title;

    @NotBlank(message = "Описание обязательно")
    @Size(min = 10, max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    private boolean isPublic = true;


    private Long categoryId;

    @NotNull(message = "Необходимо указать уровень сложности викторины")
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    // шапка квиза
    private String headImage;

    @NotNull(message = "Максимальное количество вопросов обязательно")
    @Min(value = 10, message = "Минимум 10 вопросов")
    @Max(value = 30, message = "Максимум 30 вопросов")
    private Integer maxQuestions;

    @Min(value = 2, message = "Время на викторину должно быть не менее 2 минуты")
    @Max(value = 150, message = "Время на викторину не должно превышать 2.5 часов")
    private Integer timeLimitMinutes;

    @NotNull(message = "Вопросы обязательны")
    @Size(min = 10, max = 30, message = "Количество вопросов должно быть от 10 до 30")
    @Valid
    private List<CreateQuestionDto> questions;
}