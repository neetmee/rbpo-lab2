package com.rbpo.rbpo_lab2.security;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PasswordPolicy {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[@#$%^&+=!\\-_]");

    public void validate(String password, String username) {
        if (password == null || password.length() < 10) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 10 символов");
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Пароль не должен совпадать с логином");
        }
        if (!UPPERCASE.matcher(password).find()) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы одну заглавную букву");
        }
        if (!LOWERCASE.matcher(password).find()) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы одну строчную букву");
        }
        if (!DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы одну цифру");
        }
        if (!SPECIAL.matcher(password).find()) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы один спецсимвол (@#$%^&+=!-_)");
        }
    }
}