// dossier/service/DossierWorkflowService.java
package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.AjouterDossierPersonneRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.OuvrirDossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.AjouterDossierPersonneResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.OuvrirDossierResponse;

import java.util.UUID;

public interface DossierWorkflowService {

    OuvrirDossierResponse ouvrirDossier(OuvrirDossierRequest request);

    // Ajoute un dossier a une personne deja existante, avec son implication
    // dans ce dossier (statut judiciaire, autorite, reference) et une liste
    // d'au moins un fait reproche. Toute la creation est transactionnelle.
    AjouterDossierPersonneResponse ajouterDossierAPersonne(UUID personneId, AjouterDossierPersonneRequest request);
}
