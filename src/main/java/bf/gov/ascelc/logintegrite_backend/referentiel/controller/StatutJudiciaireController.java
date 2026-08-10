package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.StatutJudiciaireResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.StatutJudiciaireService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/statuts-judiciaires")
@RequiredArgsConstructor
public class StatutJudiciaireController {

    private final StatutJudiciaireService service;

    @Operation(summary = "Lister tous les statuts judiciaires")
    @GetMapping
    public List<StatutJudiciaireResponse> lister() {
        return service.lister();
    }

    @Operation(summary = "Creer un nouveau statut judiciaire")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StatutJudiciaireResponse creer(@Valid @RequestBody StatutJudiciaireRequest request) {
        return service.creer(request);
    }

    @Operation(summary = "Modifier un statut judiciaire existant")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public StatutJudiciaireResponse modifier(@PathVariable UUID id, @Valid @RequestBody StatutJudiciaireRequest request) {
        return service.modifier(id, request);
    }

    @Operation(summary = "Supprimer un statut judiciaire")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
