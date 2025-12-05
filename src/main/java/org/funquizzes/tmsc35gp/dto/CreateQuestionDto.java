package org.funquizzes.tmsc35gp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionDto {

    private String text;
    private String firstOption;
    private String secondOption;
    private String thirdOption;
    private String fourthOption;

    private Integer correctAnswerIndex;
    private Integer timeLimitSeconds = 30;
}
