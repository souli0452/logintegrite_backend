package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.RoleImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.RoleImplicationResponse;

import java.util.List;
import java.util.UUID;

public interface RoleImplicationService {
    List<RoleImplicationResponse> lister();
    RoleImplicationResponse creer(RoleImplicationRequest request);
    RoleImplicationResponse modifier(UUID id, RoleImplicationRequest request);
    void supprimer(UUID id);
}
