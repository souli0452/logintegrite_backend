package bf.gov.ascelc.logintegrite_backend.audit.entity;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Evenement de securite releve sur le poste d'un utilisateur. Ajout seulement (voir V8). */
@Entity
@Table(name = "journal_securite", schema = "audit")
@Getter
@Setter
public class JournalSecurite {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "date_evenement", nullable = false, updatable = false)
    private Instant dateEvenement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, updatable = false)
    private Utilisateur utilisateur;

    @Column(name = "type_evenement", nullable = false, updatable = false, length = 40)
    private String typeEvenement;

    @Column(name = "page", updatable = false)
    private String page;

    @Column(name = "detail", updatable = false, length = 500)
    private String detail;

    @Column(name = "adresse_ip", updatable = false, length = 64)
    private String adresseIp;

    @Column(name = "user_agent", updatable = false, length = 400)
    private String userAgent;
}
