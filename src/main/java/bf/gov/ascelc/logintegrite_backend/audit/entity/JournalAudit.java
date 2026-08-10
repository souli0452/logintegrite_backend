// audit/entity/JournalAudit.java (complet, corrigé)
package bf.gov.ascelc.logintegrite_backend.audit.entity;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "journal_audit", schema = "audit")
@IdClass(JournalAuditId.class)
@Getter
@Setter
public class JournalAudit {

    // Aucune generation (ni @GeneratedValue ni @Generated) possible sur un
    // composant de cle composite - limitation stricte de Hibernate, confirmee
    // par l'erreur au demarrage. UUID affecte explicitement en Java.
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", updatable = false)
    private Utilisateur utilisateur;

    @Column(name = "action", nullable = false, updatable = false)
    private String action;

    @Column(name = "entite_cible", nullable = false, updatable = false)
    private String entiteCible;

    @Column(name = "entite_cible_id", updatable = false)
    private UUID entiteCibleId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valeur_avant", updatable = false)
    private String valeurAvant;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valeur_apres", updatable = false)
    private String valeurApres;

    // Ces deux-la ne font PAS partie de la cle - @Generated reste valide.
    @Generated(event = EventType.INSERT)
    @Column(name = "hash_precedent", insertable = false, updatable = false)
    private String hashPrecedent;

    @Generated(event = EventType.INSERT)
    @Column(name = "hash_actuel", insertable = false, updatable = false)
    private String hashActuel;

    @Id
    @Column(name = "date_action", updatable = false)
    private Instant dateAction;

    @Column(name = "adresse_ip", updatable = false)
    private String adresseIp;
}
