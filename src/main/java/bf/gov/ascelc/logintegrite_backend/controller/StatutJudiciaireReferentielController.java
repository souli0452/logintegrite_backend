package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireReferentielRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatutJudiciaireReferentielResponse;
import bf.gov.ascelc.logintegrite_backend.service.StatutJudiciaireReferentielService;
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
// Racine : /api/v1/referentiel/statuts-judiciaires
@RequestMapping(ApiURLs.REFERENTIEL + ApiURLs.REFERENTIEL_STATUTS_JUDICIAIRES)
@RequiredArgsConstructor
public class StatutJudiciaireReferentielController {

    private final StatutJudiciaireReferentielService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<StatutJudiciaireReferentielResponse> create(@Valid @RequestBody StatutJudiciaireReferentielRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<StatutJudiciaireReferentielResponse> update(@PathVariable UUID id, @Valid @RequestBody StatutJudiciaireReferentielRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<StatutJudiciaireReferentielResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Liste complète (écran d'administration du référentiel)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<List<StatutJudiciaireReferentielResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Liste restreinte aux statuts actifs : alimente le <select> du formulaire d'historique
    @GetMapping("/actifs")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<List<StatutJudiciaireReferentielResponse>> getAllActifs() {
        return ResponseEntity.ok(service.getAllActifs());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}