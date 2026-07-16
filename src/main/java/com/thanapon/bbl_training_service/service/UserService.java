package com.thanapon.bbl_training_service.service;

import java.util.List;

import com.thanapon.bbl_training_service.dto.request.UserCreateRequestDto;
import com.thanapon.bbl_training_service.dto.request.UserUpdateRequestDto;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;

public interface UserService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(long userId);

    UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto);

    UserResponseDto updateUserById(long userId, UserUpdateRequestDto userUpdateRequestDto);

    void deleteUserById(long userId);
}
