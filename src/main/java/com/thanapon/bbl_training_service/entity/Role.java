package com.thanapon.bbl_training_service.entity;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("admin"),
    USER("user");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static Optional<Role> fromValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.value.equalsIgnoreCase(value))
                .findFirst();
    }
}
