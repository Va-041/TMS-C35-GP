package org.funquizzes.tmsc35gp.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateQuestionDto extends CreateQuestionDto {
    private Long id; // ID вопроса для редактирования
}
