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

    // для вопросов с выбором
    @AssertTrue(message = "Для вопросов с выбором должен быть указан хотя бы один правильный ответ")
    public boolean isChoiceAnswerValid() {
        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.TRUE_FALSE) {
            return correctAnswers != null && !correctAnswers.isEmpty();
        }
        return true;
    }

    private String correctTextAnswer;
    private Boolean caseSensitive = false;

    // для текстовых вопросов
    @AssertTrue(message = "Для текстового вопроса должен быть указан правильный ответ")
    public boolean isTextAnswerValid() {
        if (type == QuestionType.TEXT_INPUT) {
            return correctTextAnswer != null && !correctTextAnswer.trim().isEmpty();
        }
        return true;
    }

    @NotNull(message = "Количество баллов обязательно")
    @Min(value = 50, message = "Минимум 50 баллов")
    @Max(value = 1000, message = "Максимум 1000 баллов")
    private Integer points;

    @Min(value = 10, message = "Минимальное время: 10 секунд")
    @Max(value = 300, message = "Максимальное время: 300 секунд (5 минут)")
    private Integer timeLimitSeconds = 30;

    private Integer questionIndex = 0;
}