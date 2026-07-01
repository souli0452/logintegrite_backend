package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.PieceJointeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PieceJointeResponse;
import bf.gov.ascelc.logintegrite_backend.service.PieceJointeService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.API_V1_ROOT + "/pieces-jointes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
public class PieceJointeController {

    private final PieceJointeService service;

    // MODIFIÉ : infractionId ajouté en paramètre optionnel
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PieceJointeResponse> uploadFichier(
            @RequestParam(value = "ficheId", required = false) UUID ficheId,
            @RequestParam(value = "infractionId", required = false) UUID infractionId,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(service.enregistrerFichierPhysique(ficheId, infractionId, file), HttpStatus.CREATED);
    }

    @PostMapping
    public ResponseEntity<PieceJointeResponse> create(@Valid @RequestBody PieceJointeRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PieceJointeResponse> update(@PathVariable UUID id, @Valid @RequestBody PieceJointeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PieceJointeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PieceJointeResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/fiche/{ficheId}")
    public ResponseEntity<List<PieceJointeResponse>> getByFicheId(@PathVariable UUID ficheId) {
        return ResponseEntity.ok(service.getByFicheId(ficheId));
    }

    // AJOUT : pièces jointes d'une infraction précise
    @GetMapping("/infraction/{infractionId}")
    public ResponseEntity<List<PieceJointeResponse>> getByInfractionId(@PathVariable UUID infractionId) {
        return ResponseEntity.ok(service.getByInfractionId(infractionId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}