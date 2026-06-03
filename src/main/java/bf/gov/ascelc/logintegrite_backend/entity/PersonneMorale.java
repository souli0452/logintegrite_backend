package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Personne morale épinglée (entreprise, ONG, coopérative...).
 * Hérite de FicheMiseEnCause — partage la même table
 * via SINGLE_TABLE inheritance.
 *
 * L'IFU (Identifiant Fiscal Unique) est l'identifiant
 * métier — contrainte d'unicité via index partiel PostgreSQL.
 */
@Entity
@DiscriminatorValue("PERSONNE_MORALE")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PersonneMorale extends FicheMiseEnCause {

    /** Dénomination officielle de la structure */
    @Column(nullable = true, length = 300)
    private String raisonSociale;

    /** Sigle ou acronyme (ex: SONABEL, ONEA) */
    @Column(length = 50)
    private String sigle;

    /**
     * Identifiant Fiscal Unique — identifiant métier Burkina Faso.
     * Unicité assurée par index partiel sur
     * type_fiche = 'PERSONNE_MORALE'.
     */
    @Column(length = 50)
    private String ifu;

    /**
     * Catégorie juridique de la structure.
     * Valeurs : ENTREPRISE, ONG, ASSOCIATION, COOPERATIVE,
     *           ETABLISSEMENT_PUBLIC, SOCIETE_ETAT, AUTRE
     */
    @Column(length = 50)
    private String typeStructure;

    /** Dirigeant ou représentant légal au moment des faits */
    @Column(length = 200)
    private String nomResponsable;

    @Column(length = 200)
    private String fonctionResponsable;
}
