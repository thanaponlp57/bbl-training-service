package com.thanapon.bbl_training_service.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thanapon.bbl_training_service.entity.Role;

import jakarta.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleValidatorTest {

    private final RoleValidator validator = new RoleValidator();

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Test
    void isValid_shouldReturnTrue_whenValueIsNull() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void isValid_shouldReturnTrue_whenValueIsKnownRole() {
        assertThat(validator.isValid("admin", null)).isTrue();
        assertThat(validator.isValid("user", null)).isTrue();
    }

    @Test
    void isValid_shouldReturnTrue_whenValueDiffersOnlyInCase() {
        assertThat(validator.isValid("ADMIN", null)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenValueIsUnknownRole() {
        given(context.buildConstraintViolationWithTemplate(anyString()))
                .willReturn(violationBuilder);

        assertThat(validator.isValid("manager", context)).isFalse();
    }

    @Test
    void isValid_shouldBuildMessageFromRoleEnumValues_whenValueIsUnknown() {
        given(context.buildConstraintViolationWithTemplate(anyString()))
                .willReturn(violationBuilder);

        validator.isValid("manager", context);

        verify(context).disableDefaultConstraintViolation();
        for (Role role : Role.values()) {
            verify(context).buildConstraintViolationWithTemplate(
                    contains(role.getValue()));
        }
    }
}
