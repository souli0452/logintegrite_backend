package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.dto.request.UtilisateurCreationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.UtilisateurResponse;

import java.util.List;
import java.util.UUID;

public interface UtilisateurService {
    List<UtilisateurResponse> lister();
    UtilisateurResponse obtenir(UUID id);
    UtilisateurResponse creer(UtilisateurCreationRequest request);          
    UtilisateurResponse modifierActivation(UUID id, boolean actif);          
    void supprimer(UUID id);                                                
    UtilisateurResponse attribuerRole(UUID utilisateurId, UUID roleHabilitationId);
    UtilisateurResponse retirerRole(UUID utilisateurId, UUID roleHabilitationId);
}
