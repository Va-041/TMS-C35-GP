package org.funquizzes.tmsc35gp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {

    @NotBlank(message = "Текущий пароль обязателен для заполнения")
    private String currentPassword;

    @NotBlank(message = "Новый пароль обязателен для заполнения")
    @Size(min = 6, message = "Новый пароль должен содержать минимум 6 символов")
    private String newPassword;

    @NotBlank(message = "Подтверждение пароля обязательно")
    private String confirmPassword;
}