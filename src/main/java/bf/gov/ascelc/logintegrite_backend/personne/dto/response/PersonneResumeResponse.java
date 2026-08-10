package bf.gov.ascelc.logintegrite_backend.personne.dto.response;

import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutAncrage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PersonneResumeResponse {
    private UUID id;
    private TypePersonne typePersonne;
    private String nomAffichage;
    private StatutAncrage statutAncrage;
    private int nombreDossiersValides;

    // Date d'inscription de la personne dans le systeme (utile pour tri
    // et affichage colonne "Enregistree le" dans la liste)
    private Instant dateCreation;
}
