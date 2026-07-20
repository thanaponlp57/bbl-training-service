package com.thanapon.bbl_training_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshRequestDto {

    @NotBlank(message = "refresh_token must not be blank")
    private final String refreshToken;
}
