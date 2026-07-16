package com.thanapon.bbl_training_service.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class DiagTest {
    @Autowired
    private DiagRepo diagRepo;
    @Autowired
    private TestEntityManager em;

    @Test
    void printTimes() {
        DiagEntity e = new DiagEntity();
        DiagEntity saved = diagRepo.saveAndFlush(e);
        System.out.println("createdAt=" + saved.getCreatedAt());
        System.out.println("updatedAt=" + saved.getUpdatedAt());
        System.out.println("equal=" + saved.getCreatedAt().equals(saved.getUpdatedAt()));
    }

    @Test
    void concurrentUpdate_shouldThrow() {
        DiagEntity e = new DiagEntity();
        DiagEntity saved = diagRepo.saveAndFlush(e);
        em.flush(); em.clear();

        DiagEntity r1 = diagRepo.findById(saved.getId()).orElseThrow();
        r1.setNewEntity(false);
        em.detach(r1);
        DiagEntity r2 = diagRepo.findById(saved.getId()).orElseThrow();
        r2.setNewEntity(false);
        em.detach(r2);

        r1.setName("first");
        diagRepo.saveAndFlush(r1);
        em.clear();

        r2.setName("second");
        assertThatThrownBy(() -> diagRepo.saveAndFlush(r2))
            .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
