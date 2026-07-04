package com.thanapon.bbl_training_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRequestDto {

    @NotBlank(message = "name must not be blank")
    private final String name;

    @NotBlank(message = "username must not be blank")
    private final String username;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private final String email;

    private final String phone;

    private final String website;
}
