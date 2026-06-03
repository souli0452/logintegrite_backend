package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_audit")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    private Long ressourceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 45)
    private String adresseIp;

    @Column(length = 20)
    private String statut = "SUCCES";

    @Column(nullable = false)
    private LocalDateTime horodatage;

    @PrePersist
    public void prePersist() {
        if (horodatage == null)
            horodatage = LocalDateTime.now();
    }

    public enum ActionAudit {
        CONNEXION, DECONNEXION, CREATION, MODIFICATION,
        SUPPRESSION, CONSULTATION, EXPORT,
        VALIDATION, REJET, ARCHIVAGE
    }
}
