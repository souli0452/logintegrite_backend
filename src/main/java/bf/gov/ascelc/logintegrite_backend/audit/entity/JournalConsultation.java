// audit/entity/JournalConsultation.java (complet, corrigé)
package bf.gov.ascelc.logintegrite_backend.audit.entity;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "journal_consultation", schema = "audit")
@IdClass(JournalConsultationId.class)
@Getter
@Setter
public class JournalConsultation {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", updatable = false)
    private Utilisateur utilisateur;

    @Column(name = "entite_consultee", nullable = false, updatable = false)
    private String entiteConsultee;

    @Column(name = "entite_consultee_id", nullable = false, updatable = false)
    private UUID entiteConsulteeId;

    @Id
    @Column(name = "date_consultation", updatable = false)
    private Instant dateConsultation;

    @Column(name = "adresse_ip", updatable = false)
    private String adresseIp;
}
