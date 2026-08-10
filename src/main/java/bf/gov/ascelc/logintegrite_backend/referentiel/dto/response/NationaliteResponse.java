package bf.gov.ascelc.logintegrite_backend.referentiel.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NationaliteResponse {
    private UUID id;
    private String libelle;
    private String codeIso;
    private boolean actif;
}
