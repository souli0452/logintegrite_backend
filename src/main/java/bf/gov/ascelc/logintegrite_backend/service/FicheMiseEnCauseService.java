package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;

/**
 * Contrat commun pour les opérations de workflow
 * applicables aux deux types (PP et PM).
 */
public interface FicheMiseEnCauseService {

    FicheMiseEnCause consulter(Long id);

    /** Soumet la fiche : BROUILLON/REJETEE → EN_ATTENTE */
    FicheMiseEnCause soumettre(Long id, String agentId);

    /** Valide : EN_ATTENTE → ACTIVE (Validateur/Admin) */
    FicheMiseEnCause valider(Long id, String validateurId);

    /** Rejette avec motif : EN_ATTENTE → REJETEE */
    FicheMiseEnCause rejeter(Long id, String motif, String validateurId);

    /** Soft-delete logique : ACTIVE → ARCHIVEE */
    FicheMiseEnCause archiver(Long id);

    /**
     * Modifie le statut judiciaire et crée une entrée détaillée dans
     * HistoriqueStatut. Remet automatiquement la fiche en EN_ATTENTE pour validation.
     */
    FicheMiseEnCause modifierStatutJudiciaire(
        Long id,
        StatutJudiciaireRequest request,
        String agentId
    );

    long countEnAttente();
}
