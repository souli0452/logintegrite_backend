package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.InfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.InfractionResponse;
import bf.gov.ascelc.logintegrite_backend.service.InfractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/infractions")
@RequiredArgsConstructor
public class InfractionController {

    private final InfractionService service;

    @PostMapping
    public ResponseEntity<InfractionResponse> create(@Valid @RequestBody InfractionRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InfractionResponse> update(@PathVariable UUID id, @Valid @RequestBody InfractionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InfractionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<InfractionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/fiche/{ficheId}")
    public ResponseEntity<List<InfractionResponse>> getByFicheId(@PathVariable UUID ficheId) {
        return ResponseEntity.ok(service.getByFicheId(ficheId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}