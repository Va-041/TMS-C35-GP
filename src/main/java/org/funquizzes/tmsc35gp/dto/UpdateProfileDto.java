package org.funquizzes.tmsc35gp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDto {

    @NotBlank(message = "Имя обязательно для заполнения")
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String name;

    @NotBlank(message = "Имя пользователя обязательно для заполнения")
    @Size(min = 3, max = 30, message = "Имя пользователя должно содержать от 3 до 30 символов")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Имя пользователя может содержать только буквы, цифры и символ подчеркивания")
    private String username;

    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Введите корректный email адрес")
    private String email;

    @Size(max = 500, message = "Биография не должна превышать 500 символов")
    private String biography;

    private boolean isPublicProfile;

    @URL(message = "Введите корректный URL адрес для аватарки")
    private String avatarUrl;
}