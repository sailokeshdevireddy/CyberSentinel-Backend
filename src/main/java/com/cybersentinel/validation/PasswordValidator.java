package com.cybersentinel.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        if (password.length() < 8) {
            return false;
        }

        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        return bytes.length <= 72;
    }
}
