package com.thanapon.bbl_training_service.validation;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.thanapon.bbl_training_service.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UniqueUsernameValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PathExcludedIdResolver PathExcludedIdResolver;

    private static final class AnnotatedFields {
        @UniqueUsername
        String onCreate;

        @UniqueUsername(exclude = "id")
        String onUpdate;
    }

    private UniqueUsernameValidator validator(String fieldName) throws NoSuchFieldException {
        Field field = AnnotatedFields.class.getDeclaredField(fieldName);
        UniqueUsernameValidator validator = new UniqueUsernameValidator(userRepository, PathExcludedIdResolver);
        validator.initialize(field.getAnnotation(UniqueUsername.class));
        return validator;
    }

    @Test
    void isValid_shouldReturnTrue_whenUsernameIsNull() throws NoSuchFieldException {
        assertThat(validator("onCreate").isValid(null, null)).isTrue();
    }

    @Test
    void isValid_shouldReturnTrue_whenUsernameNotTaken_andNoExclusion() throws NoSuchFieldException {
        given(userRepository.existsByUsername("Bret")).willReturn(false);

        assertThat(validator("onCreate").isValid("Bret", null)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenUsernameAlreadyTaken_andNoExclusion() throws NoSuchFieldException {
        given(userRepository.existsByUsername("Bret")).willReturn(true);

        assertThat(validator("onCreate").isValid("Bret", null)).isFalse();
    }

    @Test
    void isValid_shouldReturnTrue_whenUsernameBelongsOnlyToUserBeingExcluded() throws NoSuchFieldException {
        given(PathExcludedIdResolver.resolve("id")).willReturn(1L);
        given(userRepository.existsByUsernameAndIdNot("Bret", 1L)).willReturn(false);

        assertThat(validator("onUpdate").isValid("Bret", null)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenUsernameBelongsToAnotherUser() throws NoSuchFieldException {
        given(PathExcludedIdResolver.resolve("id")).willReturn(1L);
        given(userRepository.existsByUsernameAndIdNot("Bret", 1L)).willReturn(true);

        assertThat(validator("onUpdate").isValid("Bret", null)).isFalse();
    }
}
