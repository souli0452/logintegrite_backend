// dossier/dto/response/AjouterDossierPersonneResponse.java
package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AjouterDossierPersonneResponse {
    private UUID personneId;
    private UUID dossierId;
    private UUID implicationId;
    private List<UUID> faitsIds;
}
