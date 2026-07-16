package com.thanapon.bbl_training_service.service.imp;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thanapon.bbl_training_service.dto.request.UserCreateRequestDto;
import com.thanapon.bbl_training_service.dto.request.UserUpdateRequestDto;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;
import com.thanapon.bbl_training_service.entity.UserEntity;
import com.thanapon.bbl_training_service.exception.NotFoundException;
import com.thanapon.bbl_training_service.mapper.UserMapper;
import com.thanapon.bbl_training_service.repository.UserRepository;
import com.thanapon.bbl_training_service.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(long id) {
        return userMapper.toResponseDto(findUserOrThrow(id));
    }

    @Override
    public UserResponseDto createUser(UserCreateRequestDto userCreateRequestDto) {
        final UserEntity userEntity = userMapper.toEntity(userCreateRequestDto);
        userEntity.setPassword(passwordEncoder.encode(userCreateRequestDto.getPassword()));

        return userMapper.toResponseDto(userRepository.save(userEntity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponseDto updateUserById(long id, UserUpdateRequestDto userUpdateRequestDto) {
        final UserEntity userEntity = userRepository
                .findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        userMapper.updateEntityFromDto(userUpdateRequestDto, userEntity);

        return userMapper.toResponseDto(userRepository.save(userEntity));
    }

    @Override
    public void deleteUserById(long id) {
        userRepository.delete(findUserOrThrow(id));
    }

    private UserEntity findUserOrThrow(long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
