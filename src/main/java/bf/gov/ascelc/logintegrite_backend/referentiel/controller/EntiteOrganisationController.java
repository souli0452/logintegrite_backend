package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.EntiteOrganisationResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.EntiteOrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/entites-organisation")
@RequiredArgsConstructor
public class EntiteOrganisationController {

    private final EntiteOrganisationService service;

    @Operation(summary = "Lister toutes les entites d'organisation")
    @GetMapping
    public List<EntiteOrganisationResponse> lister() {
        return service.lister();
    }

    @Operation(summary = "Creer une nouvelle entite d'organisation")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntiteOrganisationResponse creer(@Valid @RequestBody EntiteOrganisationRequest request) {
        return service.creer(request);
    }

    @Operation(summary = "Modifier une entite d'organisation existante")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public EntiteOrganisationResponse modifier(@PathVariable UUID id, @Valid @RequestBody EntiteOrganisationRequest request) {
        return service.modifier(id, request);
    }

    @Operation(summary = "Supprimer une entite d'organisation")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
