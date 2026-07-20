package com.thanapon.bbl_training_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.request.RefreshRequestDto;
import com.thanapon.bbl_training_service.dto.response.AuthResponseDto;
import com.thanapon.bbl_training_service.exception.InvalidCredentialsException;
import com.thanapon.bbl_training_service.security.JwtService;
import com.thanapon.bbl_training_service.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
// JwtService must be present as a bean even though no test here exercises it directly: the
// @WebMvcTest slice still picks up JwtAuthenticationFilter (a @Component), which depends on
// JwtService, so context loading fails without this import (verified empirically).
@Import(JwtService.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void login_shouldReturnTokenPair_whenCredentialsAreValid() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("Bret", "password123");
        given(authService.login(any()))
                .willReturn(new AuthResponseDto("signed-access-token", "signed-refresh-token", 1_800L, 3_600L));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.access_token").value("signed-access-token"))
                .andExpect(jsonPath("$.data.refresh_token").value("signed-refresh-token"))
                .andExpect(jsonPath("$.data.expires_in").value(1800))
                .andExpect(jsonPath("$.data.refresh_expires_in").value(3600));
    }

    @Test
    void login_shouldReturnBadRequest_whenUsernameIsBlank() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("Bret", "wrong-password");
        given(authService.login(any())).willThrow(new InvalidCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void refresh_shouldReturnNewTokenPair_whenRefreshTokenIsValid() throws Exception {
        RefreshRequestDto requestDto = new RefreshRequestDto("old-refresh-token");
        given(authService.refresh(any()))
                .willReturn(new AuthResponseDto("new-access-token", "new-refresh-token", 1_800L, 3_600L));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.data.refresh_token").value("new-refresh-token"))
                .andExpect(jsonPath("$.data.expires_in").value(1800))
                .andExpect(jsonPath("$.data.refresh_expires_in").value(3600));
    }

    @Test
    void refresh_shouldReturnBadRequest_whenRefreshTokenIsBlank() throws Exception {
        RefreshRequestDto requestDto = new RefreshRequestDto("");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenRefreshTokenIsInvalid() throws Exception {
        RefreshRequestDto requestDto = new RefreshRequestDto("garbage");
        given(authService.refresh(any())).willThrow(new InvalidCredentialsException("Invalid or expired refresh token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
