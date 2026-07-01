package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.TypeInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.service.TypeInfractionService;
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
// Racine : /api/v1/referentiel/types-infraction
@RequestMapping(ApiURLs.REFERENTIEL + ApiURLs.REFERENTIEL_TYPES_INFRACTION)
@RequiredArgsConstructor
public class TypeInfractionController {

    private final TypeInfractionService service;

    // AJOUT : aucune protection avant
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TypeInfractionResponse> create(@Valid @RequestBody TypeInfractionRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<TypeInfractionResponse> update(@PathVariable UUID id, @Valid @RequestBody TypeInfractionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<TypeInfractionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<List<TypeInfractionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
