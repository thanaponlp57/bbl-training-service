package com.thanapon.bbl_training_service.entity;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity

// Hooks this entity into Spring Data JPA auditing (@EnableJpaAuditing in JpaAuditingConfig).
// On its own it does nothing — it lets Hibernate call this listener on insert/update,
// which is what makes @CreatedDate and @LastModifiedDate below get filled in automatically.
// The same listener also supports @CreatedBy/@LastModifiedBy for tracking who created or
// last changed the row, but those aren't used here yet — they need an AuditorAware bean
// (e.g. reading the current user from Spring Security) that this project doesn't define.
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")

// Soft delete: repository.delete(entity) normally runs a real SQL DELETE.
// @SQLDelete overrides that with an UPDATE that just stamps deleted_at, so the row is kept.
// @SQLRestriction then adds "AND deleted_at IS NULL" to every SELECT Hibernate generates for
// this entity (findAll, findById, existsByUsername, ...), so soft-deleted rows are invisible
// without every query needing to filter for it manually.
@SQLDelete(sql = "UPDATE users SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "website", nullable = true)
    private String website;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
