package com.thanapon.bbl_training_service.validation;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.thanapon.bbl_training_service.entity.Role;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoleValidator implements ConstraintValidator<ValidRole, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (Role.fromValue(value).isPresent()) {
            return true;
        }

        String validValues = Arrays.stream(Role.values())
                .map(Role::getValue)
                .collect(Collectors.joining(", "));
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("role must be one of: " + validValues)
                .addConstraintViolation();
        return false;
    }
}
