// securite/dto/response/RoleHabilitationResponse.java
package bf.gov.ascelc.logintegrite_backend.securite.dto.response;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RoleHabilitationResponse {
    private UUID id;
    private CodeRole code;
    private String libelle;
    private boolean accesVueGlobaleDossier;
}
