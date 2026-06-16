package bf.gov.ascelc.logintegrite_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ── IDENTIFIANT UNIQUE PAR UUID ──
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ── DATES D'AUDIT (AUTOMATIQUES VIA SPRING DATA) ──
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ── TRACABILITÉ UTILISATEUR (AUTOMATIQUE VIA AUDITOR AWARE / KEYCLOAK) ──
    @CreatedBy
    @Column(name = "created_by_id", updatable = false, length = 100)
    private String createdById;

    @LastModifiedBy
    @Column(name = "updated_by_id", length = 100)
    private String updatedById;

    // ── DONNÉES DE SESSION SUPPLÉMENTAIRES ──
    @Column(name = "current_user_first_name", length = 100)
    private String currentFirstName;

    @Column(name = "current_user_last_name", length = 100)
    private String currentLastName;

    @Column(name = "current_user_email", length = 150)
    private String currentUserEmail;
}