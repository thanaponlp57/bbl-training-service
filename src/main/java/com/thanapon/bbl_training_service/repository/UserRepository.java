package com.thanapon.bbl_training_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thanapon.bbl_training_service.entity.UserEntity;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    Optional<UserEntity> findByIdWithLock(@Param("id") long id);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, long id);

    Optional<UserEntity> findByUsername(String username);
}
