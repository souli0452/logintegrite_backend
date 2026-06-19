package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Personne morale épinglée (entreprise, ONG, société d'État...).
 * Hérite en cascade de AuditEntity via FicheMiseEnCause.
 * Tout est stocké dans la table unique "fiche_mise_en_cause".
 */
@Entity
@DiscriminatorValue("PERSONNE_MORALE")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PersonneMorale extends FicheMiseEnCause {

    @Column(name = "raison_sociale", length = 300)
    private String raisonSociale;

    @Column(name = "sigle", length = 50)
    private String sigle;

    @Column(name = "ifu", length = 50)
    private String ifu;

    @Column(name = "type_structure", length = 50)
    private String typeStructure;

    @Column(name = "nom_responsable", length = 200)
    private String nomResponsable;

    @Column(name = "fonction_responsable", length = 200)
    private String fonctionResponsable;
}