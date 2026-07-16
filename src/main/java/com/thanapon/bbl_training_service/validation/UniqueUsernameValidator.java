package com.thanapon.bbl_training_service.validation;

import org.springframework.stereotype.Component;

import com.thanapon.bbl_training_service.repository.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

    private final UserRepository userRepository;
    private final PathExcludedIdResolver pathExcludedIdResolver;

    private String excludePathVariable;

    public UniqueUsernameValidator(UserRepository userRepository, PathExcludedIdResolver pathExcludedIdResolver) {
        this.userRepository = userRepository;
        this.pathExcludedIdResolver = pathExcludedIdResolver;
    }

    @Override
    public void initialize(UniqueUsername constraintAnnotation) {
        this.excludePathVariable = constraintAnnotation.exclude();
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null) {
            return true;
        }

        if (excludePathVariable.isBlank()) {
            return !userRepository.existsByUsername(username);
        }

        return !userRepository.existsByUsernameAndIdNot(username, pathExcludedIdResolver.resolve(excludePathVariable));
    }
}
