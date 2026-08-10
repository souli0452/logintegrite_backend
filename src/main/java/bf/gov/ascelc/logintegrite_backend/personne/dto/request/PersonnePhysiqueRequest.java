package bf.gov.ascelc.logintegrite_backend.personne.dto.request;

import bf.gov.ascelc.logintegrite_backend.personne.enums.Sexe;
import bf.gov.ascelc.logintegrite_backend.personne.enums.SituationMatrimoniale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class PersonnePhysiqueRequest {

    @NotBlank(message = "Le nom de naissance est obligatoire")
    private String nomNaissance;

    private String nomUsage;

    @NotBlank(message = "Le ou les prenoms sont obligatoires")
    private String prenoms;

    @NotNull(message = "Le sexe est obligatoire")
    private Sexe sexe;

    @Past(message = "La date de naissance doit etre dans le passe")
    private LocalDate dateNaissance;

    private String lieuNaissance;

    // Rétrocompatibilité texte libre
    private String nationalite;

    // Référentiel ID
    private UUID nationaliteId;

    private SituationMatrimoniale situationMatrimoniale;
    private String nomConjoint;
    private String profession;
    private String matriculeFonctionPublique;
    private String gradeCategorie;
    private String adresse;
    private String telephone;
}
