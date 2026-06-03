package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

/**
 * Personne physique épinglée (agent de l'État, fonctionnaire...).
 * N'héberge QUE ses attributs propres — tout le reste est dans
 * FicheMiseEnCause.
 *
 * Contrainte métier : le matricule, quand fourni, doit être
 * unique parmi toutes les personnes physiques.
 * Implémenté via index partiel PostgreSQL (voir init.sql).
 */
@Entity
@DiscriminatorValue("PERSONNE_PHYSIQUE")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PersonnePhysique extends FicheMiseEnCause {

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = true,length = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenoms", nullable = true,length = 200)
    private String prenoms;
    private LocalDate dateNaissance;

    @Column(length = 100)
    private String lieuNaissance;

    @Column(length = 100)
    private String nationalite = "Burkinabè";

    /**
     * Identifiant métier unique.
     * La contrainte d'unicité réelle est un index partiel
     * PostgreSQL (WHERE type_fiche = 'PERSONNE_PHYSIQUE')
     * pour éviter les faux conflits avec les NULLs des
     * PersonnesMorales dans la table partagée.
     */
    @Column(length = 50)
    private String matricule;

    @Column(length = 500)
    private String photoUrl;

    @Column(length = 200)
    private String fonction;
}
