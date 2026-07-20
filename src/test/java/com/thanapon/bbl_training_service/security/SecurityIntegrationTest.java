package com.thanapon.bbl_training_service.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thanapon.bbl_training_service.dto.request.LoginRequestDto;
import com.thanapon.bbl_training_service.dto.request.RefreshRequestDto;
import com.thanapon.bbl_training_service.entity.Role;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUsers_shouldReturnUnauthorized_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsers_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsers_shouldReturnOk_whenTokenIsValid() throws Exception {
        String token = jwtService.generateAccessToken(1L, "Bret", Role.USER);

        mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_shouldReturnForbidden_whenTokenBelongsToNonAdminUser() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Leanne Graham");
        user.setUsername("Bret");
        user.setEmail("leanne@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        String token = jwtService.generateAccessToken(user.getId(), "Bret", Role.USER);

        mockMvc.perform(delete("/users/" + user.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_shouldReturnOk_whenTokenBelongsToAdminUser() throws Exception {
        UserEntity admin = new UserEntity();
        admin.setName("Admin User");
        admin.setUsername("test-admin");
        admin.setEmail("test-admin@example.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        UserEntity targetUser = new UserEntity();
        targetUser.setName("Leanne Graham");
        targetUser.setUsername("Bret");
        targetUser.setEmail("leanne@example.com");
        targetUser.setPassword(passwordEncoder.encode("password123"));
        targetUser.setRole(Role.USER);
        userRepository.save(targetUser);

        String token = jwtService.generateAccessToken(admin.getId(), "test-admin", Role.ADMIN);

        mockMvc.perform(delete("/users/" + targetUser.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldSucceed_withoutAuthorizationHeader() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Leanne Graham");
        user.setUsername("Bret");
        user.setEmail("leanne@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        LoginRequestDto requestDto = new LoginRequestDto("Bret", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_shouldReturnNewAccessTokenThatWorksOnProtectedEndpoint() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("Leanne Graham");
        user.setUsername("Bret");
        user.setEmail("leanne@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);
        long userId = user.getId();

        String refreshToken = jwtService.generateRefreshToken(userId, "Bret");
        RefreshRequestDto requestDto = new RefreshRequestDto(refreshToken);

        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        String accessToken = responseBody.path("data").path("access_token").asText();

        mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenTokenIsAnAccessTokenNotARefreshToken() throws Exception {
        String accessToken = jwtService.generateAccessToken(1L, "Bret", Role.USER);
        RefreshRequestDto requestDto = new RefreshRequestDto(accessToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}
