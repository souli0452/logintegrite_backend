// dossier/dto/response/ImplicationFaitResumeResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ImplicationFaitResumeResponse {
    private UUID id;                   // implicationFaitId (la liaison)
    private UUID implicationId;
    private UUID faitReprocheId;
    private UUID dossierId;
    private UUID personneId;

    // Contexte pour affichage
    private String numeroDossier;
    private String intituleDossier;
    private String typeInfractionLibelle;
    private String faitDescription;
    private String faitDateFaits;      // ISO date
    private String statutValidation;
}
