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
import com.thanapon.bbl_training_service.dto.request.RefreshRequestDto;
import com.thanapon.bbl_training_service.dto.response.AuthResponseDto;
import com.thanapon.bbl_training_service.entity.Role;
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
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void login_shouldReturnBothTokens_whenCredentialsAreValid() {
        UserEntity user = userWithPassword("password123");
        given(userRepository.findByUsername("Bret")).willReturn(Optional.of(user));
        given(jwtService.generateAccessToken(1L, "Bret", Role.USER)).willReturn("signed-access-token");
        given(jwtService.generateRefreshToken(1L, "Bret")).willReturn("signed-refresh-token");
        given(jwtService.getExpirationMs()).willReturn(1_800_000L);
        given(jwtService.getRefreshExpirationMs()).willReturn(3_600_000L);
        LoginRequestDto requestDto = new LoginRequestDto("Bret", "password123");

        AuthResponseDto result = authServiceImp.login(requestDto);

        assertThat(result.getAccessToken()).isEqualTo("signed-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("signed-refresh-token");
        assertThat(result.getExpiresIn()).isEqualTo(1_800L);
        assertThat(result.getRefreshExpiresIn()).isEqualTo(3_600L);
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

    @Test
    void refresh_shouldReturnNewTokenPair_whenRefreshTokenIsValid() {
        UserEntity user = userWithPassword("password123");
        given(jwtService.isTokenValid("old-refresh-token")).willReturn(true);
        given(jwtService.isRefreshToken("old-refresh-token")).willReturn(true);
        given(jwtService.extractUserId("old-refresh-token")).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtService.generateAccessToken(1L, "Bret", Role.USER)).willReturn("new-access-token");
        given(jwtService.generateRefreshToken(1L, "Bret")).willReturn("new-refresh-token");
        given(jwtService.getExpirationMs()).willReturn(1_800_000L);
        given(jwtService.getRefreshExpirationMs()).willReturn(3_600_000L);
        RefreshRequestDto requestDto = new RefreshRequestDto("old-refresh-token");

        AuthResponseDto result = authServiceImp.refresh(requestDto);

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(result.getExpiresIn()).isEqualTo(1_800L);
        assertThat(result.getRefreshExpiresIn()).isEqualTo(3_600L);
    }

    @Test
    void refresh_shouldThrowInvalidCredentialsException_whenTokenIsInvalidOrExpired() {
        given(jwtService.isTokenValid("garbage")).willReturn(false);
        RefreshRequestDto requestDto = new RefreshRequestDto("garbage");

        assertThatThrownBy(() -> authServiceImp.refresh(requestDto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_shouldThrowInvalidCredentialsException_whenTokenIsAnAccessTokenNotARefreshToken() {
        given(jwtService.isTokenValid("access-token")).willReturn(true);
        given(jwtService.isRefreshToken("access-token")).willReturn(false);
        RefreshRequestDto requestDto = new RefreshRequestDto("access-token");

        assertThatThrownBy(() -> authServiceImp.refresh(requestDto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_shouldThrowInvalidCredentialsException_whenUserNoLongerExists() {
        given(jwtService.isTokenValid("old-refresh-token")).willReturn(true);
        given(jwtService.isRefreshToken("old-refresh-token")).willReturn(true);
        given(jwtService.extractUserId("old-refresh-token")).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        RefreshRequestDto requestDto = new RefreshRequestDto("old-refresh-token");

        assertThatThrownBy(() -> authServiceImp.refresh(requestDto))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
