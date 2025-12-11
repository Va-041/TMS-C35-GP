package org.funquizzes.tmsc35gp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // изображение к вопросу

    @Column(nullable = false, length = 1000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type = QuestionType.SINGLE_CHOICE;

    // вопросы с вариантами ответов
    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text", length = 500)
    private List<String> options = new ArrayList<>();

    // изображения для вариантов ответов
    @ElementCollection
    @CollectionTable(name = "question_option_images", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_image")
    private List<String> optionsImage = new ArrayList<>();

    // правильные ответы (индексы вариантов / текст для текстовых ответов)
    @ElementCollection
    @CollectionTable(name = "question_correct_answers", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "correct_answer")
    private List<String> correctAnswers = new ArrayList<>();


    // для текстовых вопросов
    private String correctTextAnswer;
    //учёт регистра для текстовых - нет
    private Boolean caseSensitive = false;

    // баллы за правильный ответ (временно без учета подробностей)
    private Integer points = 100;
    private Integer timeLimitSeconds = 30;
    // порядок вопроса в викторине
    private Integer questionIndex = 0;
    // возможно стоит сделать функцию рандомного порядка вопросов по желанию пользователя при создании викторины
    // в виде флага. Если неактивен, то как пользователь выставил вопросы так и будут идти индексы, при создании он сможет
    // поменять порядок вопросов (отдельное поле). Если флаг активен, то несмотря на выставленные индексы вопросы
    // должны отображаться в хаотичном порядке


    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}
