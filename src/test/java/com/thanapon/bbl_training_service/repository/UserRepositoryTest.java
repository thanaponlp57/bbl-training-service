package com.thanapon.bbl_training_service.repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.thanapon.bbl_training_service.config.JpaAuditingConfig;
import com.thanapon.bbl_training_service.config.SecurityAuditorAware;
import com.thanapon.bbl_training_service.entity.Role;
import com.thanapon.bbl_training_service.entity.UserEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, UserRepositoryTest.SecurityAuditorAwareTestConfig.class})
class UserRepositoryTest {

    // JpaAuditingConfig wires @EnableJpaAuditing(auditorAwareRef = "securityAuditorAware"), which resolves
    // its AuditorAware collaborator by bean *name*. In the real application SecurityAuditorAware is picked
    // up by component scanning, which names it "securityAuditorAware" (default AnnotationBeanNameGenerator).
    // Under @DataJpaTest, classes brought in via @Import on the test class are registered by Spring Boot's
    // test context loader using a fully-qualified-class-name bean naming strategy instead, so
    // @Import(SecurityAuditorAware.class) directly would register it as
    // "com.thanapon.bbl_training_service.config.SecurityAuditorAware" and auditorAwareRef would fail to
    // resolve. Defining it as an explicit @Bean method below pins the bean name to "securityAuditorAware"
    // regardless of that import-naming behavior.
    @TestConfiguration
    static class SecurityAuditorAwareTestConfig {
        @Bean
        SecurityAuditorAware securityAuditorAware() {
            return new SecurityAuditorAware();
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private UserEntity persistUser(String username) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("Leanne Graham");
        userEntity.setUsername(username);
        userEntity.setEmail(username + "@example.com");
        userEntity.setPassword("hashed-password");
        userEntity.setRole(Role.USER);
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByIdWithLock_shouldBlockConcurrentTransaction_untilFirstTransactionCommits() throws InterruptedException {
        // Uses a username distinct from the other tests' "Bret": this test disables the default
        // @DataJpaTest rollback (see below), so the row it creates is actually committed and
        // must not collide with rows other tests insert under their own rolled-back transactions.
        UserEntity persisted = persistUser("BretLockTest");
        long id = persisted.getId();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        CountDownLatch firstTxHoldsLock = new CountDownLatch(1);
        CountDownLatch releaseFirstTx = new CountDownLatch(1);
        AtomicLong secondTxAcquiredAtNanos = new AtomicLong();

        Thread firstTx = new Thread(() -> txTemplate.executeWithoutResult(status -> {
            userRepository.findByIdWithLock(id).orElseThrow();
            firstTxHoldsLock.countDown();
            awaitUpTo5Seconds(releaseFirstTx);
        }));

        Thread secondTx = new Thread(() -> {
            awaitUpTo5Seconds(firstTxHoldsLock);
            txTemplate.executeWithoutResult(status -> {
                userRepository.findByIdWithLock(id).orElseThrow();
                secondTxAcquiredAtNanos.set(System.nanoTime());
            });
        });

        firstTx.start();
        assertThat(firstTxHoldsLock.await(5, TimeUnit.SECONDS)).isTrue();
        secondTx.start();

        // Give the second transaction time to attempt findByIdWithLock and start blocking on it.
        Thread.sleep(300);
        long releasedAtNanos = System.nanoTime();
        releaseFirstTx.countDown();

        firstTx.join(5000);
        secondTx.join(5000);

        assertThat(secondTxAcquiredAtNanos.get()).isGreaterThanOrEqualTo(releasedAtNanos);
    }

    private static void awaitUpTo5Seconds(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

    @Test
    void delete_shouldSoftDelete_hidingUserFromQueriesButKeepingRow() {
        UserEntity persisted = persistUser("Bret");
        long id = persisted.getId();
        entityManager.flush();

        userRepository.delete(persisted);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(id)).isEmpty();
        assertThat(userRepository.existsByUsername("Bret")).isFalse();

        OffsetDateTime deletedAt = (OffsetDateTime) entityManager.getEntityManager()
                .createNativeQuery("SELECT deleted_at FROM users WHERE id = ?")
                .setParameter(1, id)
                .getSingleResult();
        assertThat(deletedAt).isNotNull();
    }

    @Test
    void save_shouldSetCreatedByAndLastModifiedBy_whenAuthenticatedUserCreatesRow() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, java.util.Collections.emptyList()));

        try {
            UserEntity persisted = persistUser("Bret");

            assertThat(persisted.getCreatedByUserId()).isEqualTo(7L);
            assertThat(persisted.getUpdatedByUserId()).isEqualTo(7L);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void save_shouldLeaveCreatedByNull_whenNoAuthenticatedUser() {
        UserEntity persisted = persistUser("Bret");

        assertThat(persisted.getCreatedByUserId()).isNull();
        assertThat(persisted.getUpdatedByUserId()).isNull();
    }
}
