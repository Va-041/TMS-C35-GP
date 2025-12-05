package org.funquizzes.tmsc35gp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Question {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private String firstOption;
    private String secondOption;
    private String thirdOption;
    private String fourthOption;

    private Integer correctAnswerIndex; // индекс правильного ответа (или множество правильных)

    private Integer timeLimitSeconds = 30;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
}
