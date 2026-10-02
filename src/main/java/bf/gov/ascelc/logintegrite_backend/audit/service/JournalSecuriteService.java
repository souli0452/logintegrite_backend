package bf.gov.ascelc.logintegrite_backend.audit.service;

import bf.gov.ascelc.logintegrite_backend.audit.dto.request.EvenementPosteRequest;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EvenementSecuriteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JournalSecuriteService {

    /** Enregistre un evenement releve sur le poste de l'utilisateur connecte. */
    void enregistrer(EvenementPosteRequest requete, String userAgent);

    /** Evenement releve par le SERVEUR (recherche de verification, demande d'export) : types libres, sans plafond. */
    void enregistrerEvenementServeur(String type, String page, String detail);

    /** Nombre d'evenements d'un type, pour l'utilisateur courant, depuis un instant (limitation du debit). */
    long compterDepuis(String type, java.time.Instant depuis);

    Page<EvenementSecuriteResponse> lister(String type, Pageable pageable);
}
