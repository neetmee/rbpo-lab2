package com.rbpo.rbpo_lab2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegisterRequest(
            @NotBlank(message = "Username обязателен")
            @Pattern(regexp = "^[A-Za-z0-9_.-]{3,50}$", message = "Username должен содержать от 3 до 50 символов без спецсимволов")
            String username,

            @NotBlank(message = "Email обязателен")
            @Email(message = "Некорректный формат email")
            String email,

            @NotBlank(message = "Password обязателен")
            @Size(max = 72, message = "Пароль не должен превышать 72 символа")
            String password
    ) {}

    public record CsrfResponse(String token, String headerName) {}
}