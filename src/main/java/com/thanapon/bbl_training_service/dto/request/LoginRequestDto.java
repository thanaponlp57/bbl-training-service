package com.thanapon.bbl_training_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "username must not be blank")
    private final String username;

    @NotBlank(message = "password must not be blank")
    private final String password;
}
