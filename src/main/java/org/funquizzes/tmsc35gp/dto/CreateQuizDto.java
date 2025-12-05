package org.funquizzes.tmsc35gp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizDto {

    private String title;
    private String description;
    private boolean isPublic = true;
    private List<CreateQuestionDto> question;
}
