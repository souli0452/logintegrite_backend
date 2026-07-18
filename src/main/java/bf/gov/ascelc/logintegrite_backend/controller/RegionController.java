package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.RegionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RegionResponse;
import bf.gov.ascelc.logintegrite_backend.service.RegionService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
// Racine : /api/v1/referentiel/regions
@RequestMapping(ApiURLs.REFERENTIEL + ApiURLs.REFERENTIEL_REGIONS)
@RequiredArgsConstructor
public class RegionController {

    private final RegionService service;


    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<RegionResponse> create(@Valid @RequestBody RegionRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<RegionResponse> update(@PathVariable UUID id, @Valid @RequestBody RegionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // Lecture ouverte à tous les rôles internes, pas de restriction nécessaire
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<RegionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<List<RegionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
    
    // AJOUT : endpoint utilisé par les listes déroulantes de saisie 
@GetMapping("/actifs")
@PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
public ResponseEntity<List<RegionResponse>> getAllActifs() {
    return ResponseEntity.ok(service.getAllActifs());
}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
