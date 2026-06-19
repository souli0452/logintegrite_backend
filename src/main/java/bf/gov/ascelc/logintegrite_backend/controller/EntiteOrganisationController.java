package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.EntiteOrganisationResponse;
import bf.gov.ascelc.logintegrite_backend.service.EntiteOrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // Ajout de l'import

@RestController
@RequestMapping("/api/entite-organisations")
@RequiredArgsConstructor
public class EntiteOrganisationController {

    private final EntiteOrganisationService service;

    @PostMapping
    public ResponseEntity<EntiteOrganisationResponse> create(@Valid @RequestBody EntiteOrganisationRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntiteOrganisationResponse> update(@PathVariable UUID id, @Valid @RequestBody EntiteOrganisationRequest request) { // Changé en UUID
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntiteOrganisationResponse> getById(@PathVariable UUID id) { // Changé en UUID
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<EntiteOrganisationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) { // Changé en UUID
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}