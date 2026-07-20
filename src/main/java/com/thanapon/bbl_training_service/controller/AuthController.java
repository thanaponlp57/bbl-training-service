package com.thanapon.bbl_training_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.request.RefreshRequestDto;
import com.thanapon.bbl_training_service.dto.response.ApiResponse;
import com.thanapon.bbl_training_service.dto.response.AuthResponseDto;
import com.thanapon.bbl_training_service.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        AuthResponseDto authResponseDto = authService.login(loginRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Login successful", authResponseDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        AuthResponseDto authResponseDto = authService.refresh(refreshRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Token refresh successful", authResponseDto));
    }
}
