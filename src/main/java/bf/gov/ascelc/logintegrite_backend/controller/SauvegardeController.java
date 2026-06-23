package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.SauvegardeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.SauvegardeResponse;
import bf.gov.ascelc.logintegrite_backend.service.SauvegardeService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs; // Importation de tes constantes
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.SAUVEGARDES) // Aligné dynamiquement sur "/api/v1/sauvegardes"
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class SauvegardeController {

    private final SauvegardeService service;

    @PostMapping("/debut")
    public ResponseEntity<SauvegardeResponse> registrarDebut(@Valid @RequestBody SauvegardeRequest request) {
        return new ResponseEntity<>(service.registrarDebut(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/fin")
    public ResponseEntity<SauvegardeResponse> registrarFin(
            @PathVariable UUID id,
            @RequestParam String statut,
            @RequestParam(required = false) LocalDateTime dateFin) {

        LocalDateTime heureFin = (dateFin != null) ? dateFin : LocalDateTime.now();
        return ResponseEntity.ok(service.registrarFin(id, statut, heureFin));
    }

    @GetMapping
    public ResponseEntity<List<SauvegardeResponse>> getHistorique() {
        return ResponseEntity.ok(service.getHistorique());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SauvegardeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }
}