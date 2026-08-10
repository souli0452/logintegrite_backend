package bf.gov.ascelc.logintegrite_backend.personne.dto.response;

import bf.gov.ascelc.logintegrite_backend.personne.enums.Sexe;
import bf.gov.ascelc.logintegrite_backend.personne.enums.SituationMatrimoniale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PersonnePhysiqueResponse {
    private UUID id;
    private String nomAffichage;
    private String nomNaissance;
    private String nomUsage;
    private String prenoms;
    private Sexe sexe;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    
    // Champs de nationalité
    private String nationalite;
    private UUID nationaliteId;
    private String nationaliteLibelle;

    private SituationMatrimoniale situationMatrimoniale;
    private String nomConjoint;
    private String profession;
    private String matriculeFonctionPublique;
    private String gradeCategorie;
    private Instant dateCreation;
    private String creeParNomComplet;
    private String adresse;
    private String telephone;
    private boolean aUnePhoto;
}
