package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.service.HistoriqueStatutService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.FICHES) // Racine : /api/fiches
@RequiredArgsConstructor
public class HistoriqueStatutController {

    private final HistoriqueStatutService historiqueService;

    @PostMapping("/statut-judiciaire")
    public ResponseEntity<HistoriqueStatutResponse> changerStatutJudiciaire(
            @Valid @RequestBody HistoriqueStatutRequest request) {
        return ResponseEntity.ok(historiqueService.create(request));
    }


    @GetMapping("/{id}/historique")
    public ResponseEntity<List<HistoriqueStatutResponse>> obtenirHistoriqueParFiche(
            @PathVariable UUID id) {
        return ResponseEntity.ok(historiqueService.getByFicheId(id));
    }
}