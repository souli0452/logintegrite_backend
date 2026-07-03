package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.InfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.InfractionResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PieceJointeResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.service.InfractionService;
import bf.gov.ascelc.logintegrite_backend.service.PieceJointeService;
import bf.gov.ascelc.logintegrite_backend.service.HistoriqueStatutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/infractions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
public class InfractionController {

    private final InfractionService service;
    private final PieceJointeService pieceJointeService;       // AJOUT
    private final HistoriqueStatutService historiqueStatutService; // AJOUT

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<InfractionResponse> create(@Valid @RequestBody InfractionRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
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

    // AJOUT : accès direct aux pièces jointes d'une infraction
    @GetMapping("/{id}/pieces-jointes")
    public ResponseEntity<List<PieceJointeResponse>> getPiecesJointes(@PathVariable UUID id) {
        return ResponseEntity.ok(pieceJointeService.getByInfractionId(id));
    }

    // AJOUT : accès direct à l'historique de statut d'une infraction
    @GetMapping("/{id}/historique-statuts")
    public ResponseEntity<List<HistoriqueStatutResponse>> getHistoriqueStatuts(@PathVariable UUID id) {
        return ResponseEntity.ok(historiqueStatutService.getByInfractionId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}