// dossier/dto/response/DossierResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DossierResponse {
    private UUID id;
    private String numeroDossier;
    private String intitule;
    private UUID sourceSignalementId;
    private String sourceSignalementLibelle;
    private StatutDossier statutDossier;
    private LocalDate dateOuverture;
    private LocalDate dateCloture;
    private String descriptionContexte;
}
