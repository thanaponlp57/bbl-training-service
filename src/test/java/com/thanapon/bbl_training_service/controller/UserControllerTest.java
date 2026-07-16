package com.thanapon.bbl_training_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanapon.bbl_training_service.dto.request.UserCreateRequestDto;
import com.thanapon.bbl_training_service.dto.request.UserUpdateRequestDto;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.repository.UserRepository;
import com.thanapon.bbl_training_service.security.JwtService;
import com.thanapon.bbl_training_service.service.UserService;
import com.thanapon.bbl_training_service.validation.PathExcludedIdResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({PathExcludedIdResolver.class, JwtService.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createUser_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "", "Bret", "leanne@example.com", "password123", "1-770-736-8031", "hildegard.org");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error[?(@.field == 'name')]").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenUserNameIsBlank() throws Exception {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "Leanne Graham", "", "leanne@example.com", "password123", "1-770-736-8031", "hildegard.org");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.error[?(@.field == 'username')]").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "Leanne Graham", "Bret", "not-an-email", "password123", "1-770-736-8031", "hildegard.org");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[?(@.field == 'email')]").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_shouldReturnBadRequest_whenUsernameAlreadyExists() throws Exception {
        UserCreateRequestDto requestDto = new UserCreateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "password123", "1-770-736-8031", "hildegard.org");
        given(userRepository.existsByUsername("Bret")).willReturn(true);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[?(@.field == 'username')]").exists());

        verify(userService, never()).createUser(any());
    }

    @Test
    void updateUserById_shouldSucceed_whenUsernameUnchanged() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "1-770-736-8031", "hildegard.org");
        given(userRepository.existsByUsernameAndIdNot("Bret", 1L)).willReturn(false);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserById_shouldReturnBadRequest_whenUsernameBelongsToAnotherUser() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "1-770-736-8031", "hildegard.org");
        given(userRepository.existsByUsernameAndIdNot("Bret", 1L)).willReturn(true);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[?(@.field == 'username')]").exists());

        verify(userService, never()).updateUserById(anyLong(), any());
    }

    @Test
    void updateUserById_shouldReturnConflict_whenUserWasModifiedConcurrently() throws Exception {
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto(
                "Leanne Graham", "Bret", "leanne@example.com", "1-770-736-8031", "hildegard.org");
        given(userRepository.existsByUsernameAndIdNot("Bret", 1L)).willReturn(false);
        given(userService.updateUserById(eq(1L), any()))
                .willThrow(new ObjectOptimisticLockingFailureException(UserEntity.class, 1L));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
