package com.thanapon.bbl_training_service.service.imp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.thanapon.bbl_training_service.dto.request.UserRequestDto;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.exception.NotFoundException;
import com.thanapon.bbl_training_service.repository.UserRepository;
import com.thanapon.bbl_training_service.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(long userId) {
        return toResponseDto(findUserOrThrow(userId));
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        final UserEntity userEntity = new UserEntity();
        applyRequestDto(userEntity, userRequestDto);

        return toResponseDto(userRepository.save(userEntity));
    }

    @Override
    public UserResponseDto updateUser(long userId, UserRequestDto userRequestDto) {
        final UserEntity userEntity = findUserOrThrow(userId);
        applyRequestDto(userEntity, userRequestDto);

        return toResponseDto(userRepository.save(userEntity));
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.delete(findUserOrThrow(userId));
    }

    private UserEntity findUserOrThrow(long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void applyRequestDto(UserEntity userEntity, UserRequestDto userRequestDto) {
        userEntity.setName(userRequestDto.getName());
        userEntity.setUsername(userRequestDto.getUsername());
        userEntity.setEmail(userRequestDto.getEmail());
        userEntity.setPhone(userRequestDto.getPhone());
        userEntity.setWebsite(userRequestDto.getWebsite());
    }

    private UserResponseDto toResponseDto(UserEntity userEntity) {
        return new UserResponseDto(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getPhone(),
                userEntity.getWebsite());
    }
}
