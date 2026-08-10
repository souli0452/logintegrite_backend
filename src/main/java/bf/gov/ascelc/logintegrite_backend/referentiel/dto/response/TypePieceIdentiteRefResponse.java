package bf.gov.ascelc.logintegrite_backend.referentiel.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TypePieceIdentiteRefResponse {
    private UUID id;
    private String code;
    private String libelle;
    private boolean actif;
}
