package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_audit")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalAudit extends AuditEntity {

    @Column(length = 100)
    private String utilisateurId;

    @Column(length = 200)
    private String username;

    @Column(length = 50)
    private String role;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 100)
    private String ressourceType;

    @Column(length = 36)
    private String ressourceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 45)
    private String adresseIp;

    @Column(length = 20)
    @Builder.Default
    private String statut = "SUCCES";

    @Column(nullable = false)
    private LocalDateTime horodatage;

    @PrePersist
    public void prePersist() {
        if (horodatage == null)
            horodatage = LocalDateTime.now();
    }


    public JournalAudit identifiantUnique(String identifiantUnique) {
        this.username = identifiantUnique;
        return this;
    }
}