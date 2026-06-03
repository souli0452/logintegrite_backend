package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe parente abstraite — Héritage SINGLE_TABLE.
 * Centralise : ID, statuts, workflow, soft delete, audit JPA,
 * et toutes les relations communes vers Entite, Region,
 * PieceJointe, Infraction, HistoriqueStatut.
 *
 * La colonne "type_fiche" discrimine automatiquement
 * PersonnePhysique ("PERSONNE_PHYSIQUE") et
 * PersonneMorale ("PERSONNE_MORALE") dans la même table.
 */
@Entity
@Table(name = "fiche_mise_en_cause")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name = "type_fiche",
    discriminatorType = DiscriminatorType.STRING,
    length = 31
)
@EntityListeners(AuditingEntityListener.class)
// ── SOFT DELETE ────────────────────────────────────────────────
// UPDATE au lieu de DELETE physique
@SQLDelete(sql =
    "UPDATE fiche_mise_en_cause SET deleted = true, " +
    "date_modification = NOW() WHERE id = ? AND version = ?")
// Filtre automatique sur toutes les requêtes : exclut les supprimées
@SQLRestriction("deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class FicheMiseEnCause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── STATUTS ──────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_fiche", nullable = false, length = 20)
    @Builder.Default
    private StatutFiche statutFiche = StatutFiche.BROUILLON;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_judiciaire", nullable = false, length = 30)
    @Builder.Default
    private StatutJudiciaire statutJudiciaire =
        StatutJudiciaire.POURSUITE_EN_COURS;

    // ── WORKFLOW DE VALIDATION ────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @Column(nullable = false)
    @Builder.Default
    private Integer nbSoumissions = 0;

    /**
     * ID Keycloak du validateur — renseigné manuellement
     * lors de la validation (pas un audit JPA car c'est
     * l'admin/validateur, pas le modificateur courant).
     */
    @Column(length = 100)
    private String validateurId;

    private LocalDateTime dateValidation;

    // ── SOFT DELETE ───────────────────────────────────────────
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    // ── AUDIT JPA — AUTOMATIQUE VIA KeycloakAuditorAware ─────
    /**
     * @CreatedBy : Spring injecte automatiquement l'UUID Keycloak
     * de l'agent qui crée la fiche, via KeycloakAuditorAware.
     * Plus besoin de setCreateurId() dans les services.
     */
    @CreatedBy
    @Column(name = "createur_id", length = 100, updatable = false)
    private String createurId;

    /**
     * @LastModifiedBy : mis à jour automatiquement à chaque save().
     * Trace qui a fait la dernière modification.
     */
    @LastModifiedBy
    @Column(name = "modificateur_id", length = 100)
    private String modificateurId;

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @LastModifiedDate
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    // ── OPTIMISTIC LOCKING ────────────────────────────────────
    @Version
    private Integer version;

    // ── RELATIONS COMMUNES ────────────────────────────────────
    /**
     * Entité/Ministère au moment des faits.
     *
     * GESTION DES FUSIONS/SCISSIONS DE MINISTÈRES :
     * Si un ministère A est fusionné avec B pour créer C,
     * on crée l'entité C (actif=true) et on désactive A et B
     * (actif=false). Les fiches historiques conservent leur FK
     * vers A ou B — l'intégrité historique est préservée.
     * Seules les nouvelles fiches sont contraintes à utiliser
     * une entité active (validation dans le Service).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_id")
    private EntiteOrganisation entite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(
        mappedBy = "fiche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<Infraction> infractions = new ArrayList<>();

    @OneToMany(
        mappedBy = "fiche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<HistoriqueStatut> historiqueStatuts = new ArrayList<>();

    @OneToMany(
        mappedBy = "fiche",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<PieceJointe> piecesJointes = new ArrayList<>();

    // ── ENUMS ─────────────────────────────────────────────────
    public enum StatutFiche {
        BROUILLON, EN_ATTENTE, ACTIVE, REJETEE, ARCHIVEE
    }

    public enum StatutJudiciaire {
        POURSUITE_EN_COURS, JUGEMENT_RENDU, RELAXE, EN_APPEL
    }
}
