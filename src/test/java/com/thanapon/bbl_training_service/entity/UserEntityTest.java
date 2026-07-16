package com.thanapon.bbl_training_service.entity;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    private final UserEntity user = UserEntity.builder()
            .id(1L)
            .name("Somchai")
            .username("somchai")
            .email("somchai@example.com")
            .password("super-secret-password")
            .build();

    @Test
    void toString_shouldNotContainPassword() {
        String result = user.toString();

        assertThat(result).doesNotContain("super-secret-password");
        assertThat(result).contains("somchai");
    }

    @Test
    void jsonSerialization_shouldNotContainPassword() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(user);

        assertThat(json).doesNotContain("super-secret-password");
        assertThat(json).doesNotContain("password");
        assertThat(json).contains("somchai");
    }
}
