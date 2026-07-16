package com.thanapon.bbl_training_service.service;

import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.response.LoginResponseDto;

public interface AuthService {
    LoginResponseDto login(LoginRequestDto loginRequestDto);
}
