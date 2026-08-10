package bf.gov.ascelc.logintegrite_backend.referentiel.dto.response;

import bf.gov.ascelc.logintegrite_backend.referentiel.enums.NiveauZone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ZoneGeographiqueResponse {
    private UUID id;
    private String libelle;
    private NiveauZone niveau;
    private String code;

    // Info parent aplatie pour eviter les problemes de proxy Hibernate
    private UUID parentId;
    private String parentLibelle;
}
