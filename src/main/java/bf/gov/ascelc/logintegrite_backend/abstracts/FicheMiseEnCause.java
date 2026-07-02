package bf.gov.ascelc.logintegrite_backend.abstracts;

import bf.gov.ascelc.logintegrite_backend.entity.*;
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
@SQLDelete(sql = "UPDATE fiche_mise_en_cause SET deleted = true, updated_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class FicheMiseEnCause extends AuditEntity {

    @Column(name = "statut_fiche", nullable = false, length = 30)
    @Builder.Default
    private String statutFiche = "BROUILLON";

    @Column(name = "statut_judiciaire", nullable = false, length = 50)
    @Builder.Default
    private String statutJudiciaire = "POURSUITE_EN_COURS";

    @Column(columnDefinition = "TEXT")
    private String motifRejet;

    @Column(nullable = false)
    @Builder.Default
    private Integer nbSoumissions = 0;

    @Column(length = 100)
    private String validateurId;

    private LocalDateTime dateValidation;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Version
    private Integer version;

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

    // Machine à états centralisée ici. PersonnePhysique/PersonneMorale
    // héritent de ces méthodes — c'est ce qui manquait pour que
    // PersonneMoraleServiceImpl.f.soumettre(agentId) etc. compilent.

    public void soumettre(String agentId) {
        if (!"BROUILLON".equals(statutFiche) && !"REJETE".equals(statutFiche)) {
            throw new IllegalStateException(
                    "Impossible de soumettre une fiche au statut " + statutFiche);
        }
        this.statutFiche = "EN_ATTENTE_VALIDATION";
        this.nbSoumissions = (this.nbSoumissions == null ? 0 : this.nbSoumissions) + 1;
    }

    public void valider(String validateurId) {
        if (!"EN_ATTENTE_VALIDATION".equals(statutFiche)) {
            throw new IllegalStateException(
                    "Impossible de valider une fiche au statut " + statutFiche);
        }
        this.statutFiche = "ACTIVE";
        this.validateurId = validateurId;
        this.dateValidation = LocalDateTime.now();
        this.motifRejet = null;
    }

    public void rejeter(String motif, String validateurId) {
        if (!"EN_ATTENTE_VALIDATION".equals(statutFiche)) {
            throw new IllegalStateException(
                    "Impossible de rejeter une fiche au statut " + statutFiche);
        }
        this.statutFiche = "REJETE";
        this.motifRejet = motif;
        this.validateurId = validateurId;
        this.dateValidation = LocalDateTime.now();
    }

    public void archiver() {
        if (!"ACTIVE".equals(statutFiche) && !"REJETE".equals(statutFiche)) {
            throw new IllegalStateException(
                    "Impossible d'archiver une fiche au statut " + statutFiche);
        }
        this.statutFiche = "ARCHIVE";
    }
}