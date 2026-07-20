package com.thanapon.bbl_training_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthResponseDto {

    private final String accessToken;
    private final String refreshToken;
    private final long expiresIn;
    private final long refreshExpiresIn;
}
