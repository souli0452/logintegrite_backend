package bf.gov.ascelc.logintegrite_backend.referentiel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SourceSignalementResponse {
    private UUID id;
    private String libelle;
    private String description;
}
