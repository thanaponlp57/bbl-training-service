package com.thanapon.bbl_training_service.service;

import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.request.RefreshRequestDto;
import com.thanapon.bbl_training_service.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto loginRequestDto);

    AuthResponseDto refresh(RefreshRequestDto refreshRequestDto);
}
