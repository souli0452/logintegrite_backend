package bf.gov.ascelc.logintegrite_backend.audit.service;

import bf.gov.ascelc.logintegrite_backend.audit.dto.request.EvenementPosteRequest;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EvenementSecuriteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JournalSecuriteService {

    /** Enregistre un evenement releve sur le poste de l'utilisateur connecte. */
    void enregistrer(EvenementPosteRequest requete, String userAgent);

    Page<EvenementSecuriteResponse> lister(String type, Pageable pageable);
}
