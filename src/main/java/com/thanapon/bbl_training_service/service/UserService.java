package com.thanapon.bbl_training_service.service;

import java.util.List;

import com.thanapon.bbl_training_service.dto.request.UserRequestDto;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;

public interface UserService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(long userId);

    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto updateUser(long userId, UserRequestDto userRequestDto);

    void deleteUser(long userId);
}
