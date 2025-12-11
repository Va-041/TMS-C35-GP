package org.funquizzes.tmsc35gp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.funquizzes.tmsc35gp.entity.QuestionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionDto {

    @NotBlank(message = "Текст вопроса обязателен")
    @Size(max = 1000, message = "Текст вопроса не должен превышать 1000 символов")
    private String text;

    // изображение к вопросу
    private String image;

    @NotNull(message = "Необходимо указать тип вопроса")
    private QuestionType type;

    private List<String> options;
    private List<String> optionImages;

    private List<String> correctAnswers;
    private String correctTextAnswers;
    private Boolean caseSensitive = false;

    @Min(value = 50, message = "Минимальное количество баллов: 50")
    @Max(value = 1000, message = "Максимальное количество баллов: 1000")
    private Integer points = 100;

    @Min(value = 10, message = "Минимальное время: 10 секунд")
    @Max(value = 300, message = "Максимальное время: 300 секунд (5 минут)")
    private Integer timeLimitSeconds = 30;

    private Integer questionIndex = 0;
}