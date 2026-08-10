// dossier/controller/DossierController.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.DossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.OuvrirDossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.OuvrirDossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.service.DossierService;
import bf.gov.ascelc.logintegrite_backend.dossier.service.DossierWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dossiers")
@RequiredArgsConstructor
public class DossierController {

    private final DossierService service;
    private final DossierWorkflowService workflowService;

    @GetMapping
    public Page<DossierResponse> lister(Pageable pageable) { return service.lister(pageable); }

    @GetMapping("/{id}")
    public DossierResponse obtenir(@PathVariable UUID id) { return service.obtenir(id); }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DossierResponse creer(@Valid @RequestBody DossierRequest request) { return service.creer(request); }

    @PutMapping("/{id}")
    public DossierResponse modifier(@PathVariable UUID id, @Valid @RequestBody DossierRequest request) {
        return service.modifier(id, request);
    }

    @Operation(
        summary = "Ouvrir un dossier complet en une seule requete",
        description = "Cree en une transaction : Personne (existante OU nouvelle physique OU nouvelle morale), " +
                      "Dossier, Implication, et premier FaitReproche. Fournir exactement UNE des trois options de personne."
    )
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping("/ouverture")
    @ResponseStatus(HttpStatus.CREATED)
    public OuvrirDossierResponse ouvrirDossier(@Valid @RequestBody OuvrirDossierRequest request) {
        return workflowService.ouvrirDossier(request);
    }
}
