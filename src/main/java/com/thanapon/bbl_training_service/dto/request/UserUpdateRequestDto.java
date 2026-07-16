package com.thanapon.bbl_training_service.dto.request;

import com.thanapon.bbl_training_service.validation.UniqueUsername;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "name must not be blank")
    private final String name;

    @NotBlank(message = "username must not be blank")
    @UniqueUsername(exclude = "id")
    private final String username;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private final String email;

    private final String phone;

    private final String website;
}
