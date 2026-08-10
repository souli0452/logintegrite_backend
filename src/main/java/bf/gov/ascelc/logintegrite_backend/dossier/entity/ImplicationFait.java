// dossier/entity/ImplicationFait.java
package bf.gov.ascelc.logintegrite_backend.dossier.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.VersionedAuditEntity;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "implication_fait", schema = "dossiers")
@Getter
@Setter
public class ImplicationFait extends VersionedAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "implication_id", nullable = false, updatable = false)
    private Implication implication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fait_reproche_id", nullable = false, updatable = false)
    private FaitReproche faitReproche;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statut_judiciaire_id", nullable = false)
    private StatutJudiciaire statutJudiciaire;

    @Column(name = "date_statut", nullable = false)
    private LocalDate dateStatut = LocalDate.now();

    @Column(name = "commentaire", columnDefinition = "text")
    private String commentaire;
}
