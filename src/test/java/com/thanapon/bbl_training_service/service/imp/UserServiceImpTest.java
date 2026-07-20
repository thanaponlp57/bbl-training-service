package com.thanapon.bbl_training_service.service.imp;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thanapon.bbl_training_service.dto.request.UserCreateRequestDto;
import com.thanapon.bbl_training_service.dto.request.UserUpdateRequestDto;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;
import com.thanapon.bbl_training_service.entity.Role;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.exception.NotFoundException;
import com.thanapon.bbl_training_service.mapper.UserMapper;
import com.thanapon.bbl_training_service.mapper.UserMapperImpl;
import com.thanapon.bbl_training_service.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserServiceImp userServiceImp;

    @Captor
    private ArgumentCaptor<UserEntity> userEntityCaptor;

    private UserEntity existingUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("Leanne Graham");
        userEntity.setUsername("Bret");
        userEntity.setEmail("leanne@example.com");
        userEntity.setPhone("1-770-736-8031");
        userEntity.setWebsite("hildegard.org");
        return userEntity;
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(existingUser()));

        UserResponseDto result = userServiceImp.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("leanne@example.com");
    }

    @Test
    void getUserById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImp.getUserById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createUser_shouldSaveAndReturnNewUser() {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "password123", "user", "1-770-736-8031", "hildegard.org");
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> {
            UserEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserResponseDto result = userServiceImp.createUser(requestDto);

        verify(userRepository).save(userEntityCaptor.capture());
        assertThat(userEntityCaptor.getValue().getUsername()).isEqualTo("Bret");
        assertThat(userEntityCaptor.getValue().getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", userEntityCaptor.getValue().getPassword())).isTrue();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("Bret");
    }

    @Test
    void createUser_shouldSetRoleFromRequestedValue_whenRoleIsAdmin() {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "password123", "admin", "1-770-736-8031", "hildegard.org");
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        userServiceImp.createUser(requestDto);

        verify(userRepository).save(userEntityCaptor.capture());
        assertThat(userEntityCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createUser_shouldSetRoleFromRequestedValue_whenRoleIsUser() {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "password123", "user", "1-770-736-8031", "hildegard.org");
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));

        userServiceImp.createUser(requestDto);

        verify(userRepository).save(userEntityCaptor.capture());
        assertThat(userEntityCaptor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void updateUserById_shouldUpdateFieldsAndKeepId_whenUserExists() {
        given(userRepository.findByIdWithLock(1L)).willReturn(Optional.of(existingUser()));
        given(userRepository.save(any(UserEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "Leanne G. Updated", "Bret", "leanne.updated@example.com", "099-999-9999", "updated.org");

        UserResponseDto result = userServiceImp.updateUserById(1L, requestDto);

        verify(userRepository).save(userEntityCaptor.capture());
        assertThat(userEntityCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(userEntityCaptor.getValue().getName()).isEqualTo("Leanne G. Updated");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("leanne.updated@example.com");
        assertThat(result.getWebsite()).isEqualTo("updated.org");
    }

    @Test
    void updateUserById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        given(userRepository.findByIdWithLock(99L)).willReturn(Optional.empty());
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "name", "username", "email@example.com", "phone", "website");

        assertThatThrownBy(() -> userServiceImp.updateUserById(99L, requestDto))
                .isInstanceOf(NotFoundException.class);
    }
}
