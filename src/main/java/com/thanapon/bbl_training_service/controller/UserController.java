package com.thanapon.bbl_training_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanapon.bbl_training_service.dto.request.UserRequestDto;
import com.thanapon.bbl_training_service.dto.response.ApiResponse;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;
import com.thanapon.bbl_training_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {

        final List<UserResponseDto> userResponseDtos = userService.getAllUsers();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Users retrieved successfully", userResponseDtos));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable long userId) {

        final UserResponseDto userResponseDto = userService.getUserById(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Retrieve a list of all users.", userResponseDto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Valid @RequestBody UserRequestDto userRequestDto) {

        final UserResponseDto userResponseDto = userService.createUser(userRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Create a new user.", userResponseDto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable long userId,
            @Valid @RequestBody UserRequestDto userRequestDto) {

        final UserResponseDto userResponseDto = userService.updateUser(userId, userRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Update details of an existing user.", userResponseDto));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable long userId) {

        userService.deleteUser(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Delete a specific user.", null));
    }
}
