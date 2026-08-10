// securite/service/RoleHabilitationService.java
package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.dto.request.RoleHabilitationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.RoleHabilitationResponse;

import java.util.List;
import java.util.UUID;

public interface RoleHabilitationService {
    List<RoleHabilitationResponse> lister();
    RoleHabilitationResponse creer(RoleHabilitationRequest request);
    RoleHabilitationResponse modifier(UUID id, RoleHabilitationRequest request);
}
