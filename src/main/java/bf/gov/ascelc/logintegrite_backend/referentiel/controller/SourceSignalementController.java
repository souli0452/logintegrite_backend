package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.SourceSignalementRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.SourceSignalementResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.SourceSignalementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/sources-signalement")
@RequiredArgsConstructor
public class SourceSignalementController {

    private final SourceSignalementService service;

    @Operation(summary = "Lister toutes les sources de signalement")
    @GetMapping
    public List<SourceSignalementResponse> lister() {
        return service.lister();
    }

    @Operation(summary = "Creer une nouvelle source de signalement")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SourceSignalementResponse creer(@Valid @RequestBody SourceSignalementRequest request) {
        return service.creer(request);
    }

    @Operation(summary = "Modifier une source de signalement existante")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public SourceSignalementResponse modifier(@PathVariable UUID id, @Valid @RequestBody SourceSignalementRequest request) {
        return service.modifier(id, request);
    }

    @Operation(summary = "Supprimer une source de signalement")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
