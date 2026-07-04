package com.thanapon.bbl_training_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thanapon.bbl_training_service.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {}
