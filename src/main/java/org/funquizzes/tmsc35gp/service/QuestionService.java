package org.funquizzes.tmsc35gp.service;

import org.funquizzes.tmsc35gp.dto.CreateQuestionDto;
import org.funquizzes.tmsc35gp.entity.Question;
import org.funquizzes.tmsc35gp.entity.QuestionType;
import org.funquizzes.tmsc35gp.entity.Quiz;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    // создаём вопрос из DTO и связывает его с викториной
    public Question createQuestionFromDto(CreateQuestionDto dto, Quiz quiz) {
        Question question = new Question();
        question.setText(dto.getText());
        question.setImageUrl(dto.getImage());
        question.setType(dto.getType());
        question.setPoints(dto.getPoints());
        question.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        question.setQuestionIndex(dto.getQuestionIndex());
        question.setQuiz(quiz);

        // обработка вариантов ответов в зависимости от типа вопроса
        processQuestionOptions(question, dto);

        return question;
    }

    // обработка вариантов ответов и правильных ответы в зависимости от типа вопроса
    private void processQuestionOptions(Question question, CreateQuestionDto dto) {
        switch (dto.getType()) {
            case TRUE_FALSE:
                // для TRUE_FALSE создаем фиксированные варианты
                question.setOptions(List.of("Правда", "Ложь"));
                // correctAnswers содержит "0" для Правда или "1" для Ложь
                if (dto.getCorrectAnswers() != null && !dto.getCorrectAnswers().isEmpty()) {
                    question.setCorrectAnswers(dto.getCorrectAnswers());
                } else {
                    // По умолчанию устанавливаем "Правда" как правильный ответ
                    question.setCorrectAnswers(List.of("0"));
                }
                break;

            case SINGLE_CHOICE:
            case MULTIPLE_CHOICE:
                if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
                    throw new IllegalArgumentException("Для вопросов с выбором необходимо указать варианты ответов");
                }
                question.setOptions(dto.getOptions());
                question.setOptionsImage(dto.getOptionImages());

                // Валидация правильных ответов только если они есть
                if (dto.getCorrectAnswers() != null && !dto.getCorrectAnswers().isEmpty()) {
                    validateCorrectAnswers(dto.getOptions(), dto.getCorrectAnswers(), dto.getType());
                    question.setCorrectAnswers(dto.getCorrectAnswers());
                } else {
                    // Для вопросов с выбором должен быть хотя бы один правильный ответ
                    throw new IllegalArgumentException("Необходимо указать хотя бы один правильный ответ");
                }
                break;

            case TEXT_INPUT:
                if (dto.getCorrectTextAnswer() == null || dto.getCorrectTextAnswer().trim().isEmpty()) {
                    throw new IllegalArgumentException("Для текстового вопроса необходимо указать правильный ответ");
                }
                question.setCorrectTextAnswer(dto.getCorrectTextAnswer());
                question.setCaseSensitive(dto.getCaseSensitive() != null ? dto.getCaseSensitive() : false);
                break;

            default:
                throw new IllegalArgumentException("Неизвестный тип вопроса: " + dto.getType());
        }
    }

    // валидируем правильные ответы для вопросов с выбором
    private void validateCorrectAnswers(List<String> options, List<String> correctAnswers, QuestionType type) {
        // Уже проверено, что correctAnswers не null и не пустой

        if (type == QuestionType.SINGLE_CHOICE && correctAnswers.size() > 1) {
            throw new IllegalArgumentException("Для вопроса с одиночным выбором может быть только один правильный ответ");
        }

        // индексы правильных ответов находятся в пределах количества вариантов
        for (String answerIndex : correctAnswers) {
            try {
                int index = Integer.parseInt(answerIndex);
                if (index < 0 || index >= options.size()) {
                    throw new IllegalArgumentException(
                            String.format("Индекс правильного ответа %d выходит за пределы количества вариантов (%d)",
                                    index, options.size())
                    );
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный формат индекса правильного ответа: " + answerIndex);
            }
        }
    }

    //  создаём список вопросов из списка DTO
    public List<Question> createQuestionsFromDto(List<CreateQuestionDto> questionDtos, Quiz quiz) {
        return questionDtos.stream()
                .map(qDto -> createQuestionFromDto(qDto, quiz))
                .toList();
    }

    //  обновляем вопрос из DTO
    public Question updateQuestionFromDto(Question question, CreateQuestionDto dto) {
        question.setText(dto.getText());
        question.setImageUrl(dto.getImage());
        question.setType(dto.getType());
        question.setPoints(dto.getPoints());
        question.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        question.setQuestionIndex(dto.getQuestionIndex());

        // очищаем старые данные
        question.getOptions().clear();
        question.getOptionsImage().clear();
        question.getCorrectAnswers().clear();
        question.setCorrectTextAnswer(null);

        // обрабатываем новые данные
        processQuestionOptions(question, dto);

        return question;
    }


}