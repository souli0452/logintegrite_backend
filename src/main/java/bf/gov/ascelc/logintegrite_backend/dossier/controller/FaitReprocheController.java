package bf.gov.ascelc.logintegrite_backend.dossier.controller;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.FaitReprocheRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitRejeteResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.RejetFaitRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierAValiderResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitReprocheResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.service.FaitReprocheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
public class FaitReprocheController {

    private final FaitReprocheService service;

    @GetMapping("/api/v1/dossiers/{dossierId}/faits")
    public List<FaitReprocheResponse> lister(@PathVariable UUID dossierId) {
        return service.listerParDossier(dossierId);
    }
    
    @Operation(summary = "Liste les faits par statut de validation")
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @GetMapping("/api/v1/faits")
    public Page<FaitReprocheResponse> listerParStatut(
            @RequestParam StatutValidation statutValidation,
            Pageable pageable) {
        return service.listerParStatut(statutValidation, pageable);
    }
    
    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping("/api/v1/dossiers/{dossierId}/faits")
    @ResponseStatus(HttpStatus.CREATED)
    public FaitReprocheResponse creer(@PathVariable UUID dossierId, @Valid @RequestBody FaitReprocheRequest request) {
        return service.creer(dossierId, request);
    }
    
    @Operation(summary = "Valider un fait", description = "Reserve VALIDATEUR/ADMIN. Passe le statut a VALIDEE.")
    @PreAuthorize("hasAnyRole('VALIDATEUR','ADMIN')")
    @PutMapping("/api/v1/faits/{faitId}/valider")
    public FaitReprocheResponse valider(@PathVariable UUID faitId) {
        return service.valider(faitId);
    }
    
    @PreAuthorize("hasAnyRole('VALIDATEUR','ADMIN')")
    @PutMapping("/api/v1/faits/{faitId}/rejeter")
    public FaitReprocheResponse rejeter(@PathVariable UUID faitId, @Valid @RequestBody RejetFaitRequest request) {
        return service.rejeter(faitId, request);
    }
    
    @Operation(summary = "Lister les faits rejetes avec contexte enrichi",
    description = "Renvoie les faits rejetes avec dossier + personne + motif. Reserve VALIDATEUR/ADMIN.")
@PreAuthorize("hasAnyRole('VALIDATEUR','ADMIN')")
@GetMapping("/api/v1/faits/rejetes")
public List<FaitRejeteResponse> listerRejetes() {
    return service.listerRejetes();
}

@Operation(summary = "Reprendre en edition un fait rejete",
    description = "Repasse le statut du fait a EN_ATTENTE, efface le motif de rejet. Reserve AGENT/ADMIN.")
@PreAuthorize("hasAnyRole('AGENT','ADMIN')")
@PutMapping("/api/v1/faits/{faitId}/reprendre")
public FaitReprocheResponse reprendre(@PathVariable UUID faitId) {
    return service.reprendre(faitId);
}
    
    @Operation(summary = "Lister les dossiers avec des faits en attente de validation",
       description = "Vue agregee : un dossier + les faits EN_ATTENTE de ce dossier. Reserve VALIDATEUR/ADMIN.")
@PreAuthorize("hasAnyRole('VALIDATEUR','ADMIN')")
@GetMapping("/api/v1/validation/dossiers-avec-faits-en-attente")
public List<DossierAValiderResponse> listerDossiersAvecFaitsEnAttente() {
    return service.listerDossiersAvecFaitsEnAttente();
}
}
