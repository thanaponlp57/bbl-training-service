package com.thanapon.bbl_training_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponseDto {

    private final long   id;
    private final String name;
    private final String username;
    private final String email;
    private final String phone;
    private final String website;
}
