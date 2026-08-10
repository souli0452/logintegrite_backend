package bf.gov.ascelc.logintegrite_backend.referentiel.controller;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.ZoneGeographiqueRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.ZoneGeographiqueResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.ZoneGeographiqueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referentiels/zones-geographiques")
@RequiredArgsConstructor
public class ZoneGeographiqueController {

    private final ZoneGeographiqueService service;

    @Operation(summary = "Lister toutes les zones geographiques")
    @GetMapping
    public List<ZoneGeographiqueResponse> lister() {
        return service.lister();
    }

    @Operation(summary = "Creer une nouvelle zone geographique")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ZoneGeographiqueResponse creer(@Valid @RequestBody ZoneGeographiqueRequest request) {
        return service.creer(request);
    }

    @Operation(summary = "Modifier une zone geographique existante")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ZoneGeographiqueResponse modifier(@PathVariable UUID id, @Valid @RequestBody ZoneGeographiqueRequest request) {
        return service.modifier(id, request);
    }

    @Operation(summary = "Supprimer une zone geographique")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID id) {
        service.supprimer(id);
    }
}
