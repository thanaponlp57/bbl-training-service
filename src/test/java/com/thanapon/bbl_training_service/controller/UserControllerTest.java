package com.thanapon.bbl_training_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanapon.bbl_training_service.dto.request.UserRequestDto;
import com.thanapon.bbl_training_service.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createUser_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        UserRequestDto requestDto = new UserRequestDto(
                "", "Bret", "leanne@example.com", "1-770-736-8031", "hildegard.org");

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
        UserRequestDto requestDto = new UserRequestDto(
                "Leanne Graham", "", "leanne@example.com", "1-770-736-8031", "hildegard.org");

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
        UserRequestDto requestDto = new UserRequestDto(
                "Leanne Graham", "Bret", "not-an-email", "1-770-736-8031", "hildegard.org");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error[?(@.field == 'email')]").exists());

        verify(userService, never()).createUser(any());
    }
}
