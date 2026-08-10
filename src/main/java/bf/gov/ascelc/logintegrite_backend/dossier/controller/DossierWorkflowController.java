// dossier/controller/DossierWorkflowController.java
package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.AjouterDossierPersonneRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.OuvrirDossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.AjouterDossierPersonneResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.OuvrirDossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.service.DossierWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DossierWorkflowController {

    private final DossierWorkflowService service;

    // Ajouter un nouveau dossier a une personne existante (dossier + implication + faits en une transaction)
    @PostMapping("/personnes/{personneId}/dossiers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    public AjouterDossierPersonneResponse ajouterDossier(
            @PathVariable UUID personneId,
            @Valid @RequestBody AjouterDossierPersonneRequest request) {
        return service.ajouterDossierAPersonne(personneId, request);
    }
}
