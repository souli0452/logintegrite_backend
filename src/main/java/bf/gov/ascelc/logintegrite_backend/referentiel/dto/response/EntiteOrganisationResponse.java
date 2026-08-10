package bf.gov.ascelc.logintegrite_backend.referentiel.dto.response;

import bf.gov.ascelc.logintegrite_backend.referentiel.enums.NiveauEntite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class EntiteOrganisationResponse {
    private UUID id;
    private String libelle;
    private NiveauEntite niveau;

    // Info parent aplatie pour eviter les problemes de proxy Hibernate
    private UUID parentId;
    private String parentLibelle;
}
