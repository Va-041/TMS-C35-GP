package org.funquizzes.tmsc35gp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.funquizzes.tmsc35gp.entity.DifficultyLevel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuizDto {

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

    private String headImage;

    @Min(value = 10, message = "Минимальное количество вопросов: 10")
    @Max(value = 30, message = "Максимальное количество вопросов: 30")
    private Integer maxQuestions;

    @Min(value = 2, message = "Время на викторину должно быть не менее 2 минуты")
    @Max(value = 150, message = "Время на викторину не должно превышать 2.5 часов")
    private Integer timeLimitMinutes;

    @NotNull(message = "Вопросы обязательны")
    @Size(min = 10, max = 30, message = "Количество вопросов должно быть от 10 до 30")
    private List<CreateQuestionDto> questions;

    private Long id; // ID викторины для редактирования
}
