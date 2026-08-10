// securite/dto/response/UtilisateurResponse.java
package bf.gov.ascelc.logintegrite_backend.securite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UtilisateurResponse {
    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
    private List<RoleHabilitationResponse> roles;
}
