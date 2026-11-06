package com.rbpo.rbpo_lab2.dto;

import com.rbpo.rbpo_lab2.model.AccountStatus;
import com.rbpo.rbpo_lab2.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public record RegisterRequest(
            @NotBlank(message = "Логин обязателен")
            @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
            String username,

            @Email(message = "Некорректный формат email")
            String email,

            @NotBlank(message = "Пароль обязателен")
            String password
    ) {}

    public record MeResponse(
            Long id,
            String username,
            String email,
            Role role,
            AccountStatus status
    ) {}
}