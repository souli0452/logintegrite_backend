package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.service.HistoriqueStatutService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.FICHES) // Aligné dynamiquement sur "/api/v1/fiches"
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
public class HistoriqueStatutController {

    private final HistoriqueStatutService historiqueService;

    // URL finale : POST /api/v1/fiches/{ficheId}/historique-statut
    @PostMapping("/{ficheId}/historique-statut")
    public ResponseEntity<HistoriqueStatutResponse> changerStatutJudiciaire(
            @PathVariable UUID ficheId,
            @Valid @RequestBody HistoriqueStatutRequest request) {

        request.setFicheId(ficheId);
        return new ResponseEntity<>(historiqueService.create(request), HttpStatus.CREATED);
    }

    // URL finale : GET /api/v1/fiches/{id}/historique
    @GetMapping("/{id}/historique")
    public ResponseEntity<List<HistoriqueStatutResponse>> obtenirHistoriqueParFiche(
            @PathVariable UUID id) {
        return ResponseEntity.ok(historiqueService.getByFicheId(id));
    }
}
