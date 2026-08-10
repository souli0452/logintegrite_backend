package bf.gov.ascelc.logintegrite_backend.personne.controller;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PieceIdentiteRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PieceIdentiteResponse;
import bf.gov.ascelc.logintegrite_backend.personne.service.PieceIdentiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PieceIdentiteController {

    private final PieceIdentiteService service;

    @GetMapping("/api/v1/personnes/physiques/{personnePhysiqueId}/pieces-identite")
    public List<PieceIdentiteResponse> lister(@PathVariable UUID personnePhysiqueId) {
        return service.listerParPersonnePhysique(personnePhysiqueId);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @PostMapping("/api/v1/personnes/physiques/{personnePhysiqueId}/pieces-identite")
    @ResponseStatus(HttpStatus.CREATED)
    public PieceIdentiteResponse creer(@PathVariable UUID personnePhysiqueId, @Valid @RequestBody PieceIdentiteRequest request) {
        return service.creer(personnePhysiqueId, request);
    }

    @PreAuthorize("hasAnyRole('AGENT','VALIDATEUR','ADMIN')")
    @DeleteMapping("/api/v1/pieces-identite/{pieceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable UUID pieceId) {
        service.supprimer(pieceId);
    }
}
