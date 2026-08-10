// dossier/entity/Implication.java
package bf.gov.ascelc.logintegrite_backend.dossier.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.RoleImplication;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

// IdentifiableEntity seulement : implication n'a que date_creation dans le
// DDL, ni date_maj ni version - verifie ligne par ligne avant d'ecrire ceci.
@Entity
@Table(name = "implication", schema = "dossiers")
@Getter
@Setter
public class Implication extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personne_id", nullable = false, updatable = false)
    private Personne personne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private Dossier dossier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_implication_id", nullable = false)
    private RoleImplication roleImplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entite_organisation_id")
    private EntiteOrganisation entiteOrganisation;
    
    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "statut_judiciaire_id")
private StatutJudiciaire statutJudiciaire;

    @Column(name = "autorite_competente")
    private String autoriteCompetente;

    @Column(name = "reference_affaire")
    private String referenceAffaire;

    @Column(name = "fonction_occupee")
    private String fonctionOccupee;

    @Column(name = "entite_libelle_a_l_epoque")
    private String entiteLibelleALEpoque;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut = LocalDate.now();

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "observations", columnDefinition = "text")
    private String observations;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private Instant dateCreation;
}
