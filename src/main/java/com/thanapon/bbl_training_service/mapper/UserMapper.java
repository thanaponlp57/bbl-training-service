package com.thanapon.bbl_training_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.thanapon.bbl_training_service.dto.request.UserCreateRequestDto;
import com.thanapon.bbl_training_service.dto.request.UserUpdateRequestDto;
import com.thanapon.bbl_training_service.dto.response.UserResponseDto;
import com.thanapon.bbl_training_service.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    UserEntity toEntity(UserCreateRequestDto dto);

    void updateEntityFromDto(UserUpdateRequestDto dto, @MappingTarget UserEntity entity);

    UserResponseDto toResponseDto(UserEntity entity);
}
