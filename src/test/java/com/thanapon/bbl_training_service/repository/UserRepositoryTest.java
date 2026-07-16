package com.thanapon.bbl_training_service.repository;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.thanapon.bbl_training_service.config.JpaAuditingConfig;
import com.thanapon.bbl_training_service.entity.UserEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String username) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("Leanne Graham");
        userEntity.setUsername(username);
        userEntity.setEmail(username + "@example.com");
        return userRepository.save(userEntity);
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenUsernameExists() {
        persistUser("Bret");

        assertThat(userRepository.existsByUsername("Bret")).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenUsernameDoesNotExist() {
        assertThat(userRepository.existsByUsername("Bret")).isFalse();
    }

    @Test
    void existsByUsernameAndIdNot_shouldReturnFalse_whenOnlyMatchIsSameId() {
        UserEntity user = persistUser("Bret");

        assertThat(userRepository.existsByUsernameAndIdNot("Bret", user.getId())).isFalse();
    }

    @Test
    void existsByUsernameAndIdNot_shouldReturnTrue_whenAnotherUserHasSameUsername() {
        UserEntity user = persistUser("Bret");
        long otherUserId = user.getId() + 1;

        assertThat(userRepository.existsByUsernameAndIdNot("Bret", otherUserId)).isTrue();
    }

    @Test
    void save_shouldThrowOptimisticLockingFailure_whenRowWasUpdatedSinceItWasRead() {
        UserEntity persisted = persistUser("Bret");
        entityManager.flush();
        entityManager.clear();

        UserEntity firstRead = userRepository.findById(persisted.getId()).orElseThrow();
        entityManager.detach(firstRead);
        UserEntity secondRead = userRepository.findById(persisted.getId()).orElseThrow();
        entityManager.detach(secondRead);

        firstRead.setName("Updated by first request");
        userRepository.saveAndFlush(firstRead);
        entityManager.clear();

        secondRead.setName("Updated by second request");
        assertThatThrownBy(() -> userRepository.saveAndFlush(secondRead))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void save_shouldSetCreatedAt_whenUserIsCreated() {
        UserEntity persisted = persistUser("Bret");

        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    @Test
    void save_shouldNotChangeCreatedAt_whenUserIsUpdated() {
        UserEntity persisted = persistUser("Bret");
        entityManager.flush();
        entityManager.clear();
        Instant originalCreatedAt = userRepository.findById(persisted.getId()).orElseThrow().getCreatedAt();
        entityManager.clear();

        UserEntity toUpdate = userRepository.findById(persisted.getId()).orElseThrow();
        toUpdate.setName("Updated name");
        userRepository.saveAndFlush(toUpdate);
        entityManager.clear();

        UserEntity reloaded = userRepository.findById(persisted.getId()).orElseThrow();
        assertThat(reloaded.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void save_shouldSetCreatedAtEqualToUpdatedAt_whenUserIsCreated() {
        UserEntity persisted = persistUser("Bret");

        assertThat(persisted.getCreatedAt()).isEqualTo(persisted.getUpdatedAt());
    }
}
