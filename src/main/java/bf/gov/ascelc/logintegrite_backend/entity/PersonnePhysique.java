package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

/**
 * Personne physique épinglée (agent de l'État, fonctionnaire...).
 * Hérite en cascade de AuditEntity via FicheMiseEnCause.
 * Tout est stocké dans la table unique "fiche_mise_en_cause".
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
    @Column(name = "nom", length = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenoms", length = 200)
    private String prenoms;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 100)
    private String lieuNaissance;

    @Column(name = "nationalite", length = 100)
    @Builder.Default
    private String nationalite = "Burkinabè";

    @Column(name = "matricule", length = 50)
    private String matricule;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "fonction", length = 200)
    private String fonction;
}