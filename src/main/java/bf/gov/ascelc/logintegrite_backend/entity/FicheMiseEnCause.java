package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "fiche_mise_en_cause")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "type_fiche",
        discriminatorType = DiscriminatorType.STRING,
        length = 31
)
// Soft delete ciblant la bonne table avec le champ d'audit de AuditEntity (updatedAt)
@SQLDelete(sql = "UPDATE fiche_mise_en_cause SET deleted = true, updated_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class FicheMiseEnCause extends AuditEntity {

    // ── STATUTS DYNAMIQUES  ──
    @Column(name = "statut_fiche", nullable = false, length = 30)
    @Builder.Default
    private String statutFiche = "BROUILLON";

    @Column(name = "statut_judiciaire", nullable = false, length = 50)
    @Builder.Default
    private String statutJudiciaire = "POURSUITE_EN_COURS";

    // ── WORKFLOW DE VALIDATION ────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @Column(nullable = false)
    @Builder.Default
    private Integer nbSoumissions = 0;

    @Column(length = 100)
    private String validateurId;

    private LocalDateTime dateValidation;

    // ── SOFT DELETE ───────────────────────────────────────────
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    // ── OPTIMISTIC LOCKING ────────────────────────────────────
    @Version
    private Integer version;

    // ── RELATIONS COMMUNES ────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_id")
    private EntiteOrganisation entite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @OneToMany(mappedBy = "fiche", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Infraction> infractions = new ArrayList<>();

    @OneToMany(mappedBy = "fiche", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistoriqueStatut> historiqueStatuts = new ArrayList<>();

    @OneToMany(mappedBy = "fiche", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PieceJointe> piecesJointes = new ArrayList<>();
}