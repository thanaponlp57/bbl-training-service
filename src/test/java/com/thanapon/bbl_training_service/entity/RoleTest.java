package com.thanapon.bbl_training_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void fromValue_shouldReturnMatchingRole_whenValueIsKnown() {
        assertThat(Role.fromValue("admin")).contains(Role.ADMIN);
        assertThat(Role.fromValue("user")).contains(Role.USER);
    }

    @Test
    void fromValue_shouldMatchCaseInsensitively() {
        assertThat(Role.fromValue("ADMIN")).contains(Role.ADMIN);
    }

    @Test
    void fromValue_shouldReturnEmpty_whenValueIsUnknown() {
        assertThat(Role.fromValue("manager")).isEmpty();
    }
}
