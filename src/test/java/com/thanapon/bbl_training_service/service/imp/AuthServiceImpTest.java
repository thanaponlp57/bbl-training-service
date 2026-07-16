package com.thanapon.bbl_training_service.service.imp;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.response.LoginResponseDto;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.exception.InvalidCredentialsException;
import com.thanapon.bbl_training_service.repository.UserRepository;
import com.thanapon.bbl_training_service.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceImpTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImp authServiceImp;

    private UserEntity userWithPassword(String rawPassword) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("Bret");
        user.setPassword(passwordEncoder.encode(rawPassword));
        return user;
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        UserEntity user = userWithPassword("password123");
        given(userRepository.findByUsername("Bret")).willReturn(Optional.of(user));
        given(jwtService.generateToken(1L, "Bret")).willReturn("signed-jwt-token");
        LoginRequestDto requestDto = new LoginRequestDto("Bret", "password123");

        LoginResponseDto result = authServiceImp.login(requestDto);

        assertThat(result.getToken()).isEqualTo("signed-jwt-token");
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsWrong() {
        UserEntity user = userWithPassword("password123");
        given(userRepository.findByUsername("Bret")).willReturn(Optional.of(user));
        LoginRequestDto requestDto = new LoginRequestDto("Bret", "wrong-password");

        assertThatThrownBy(() -> authServiceImp.login(requestDto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenUsernameDoesNotExist() {
        given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());
        LoginRequestDto requestDto = new LoginRequestDto("unknown", "password123");

        assertThatThrownBy(() -> authServiceImp.login(requestDto))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
