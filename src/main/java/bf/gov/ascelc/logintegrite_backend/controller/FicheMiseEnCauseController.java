package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import bf.gov.ascelc.logintegrite_backend.mapper.FicheMiseEnCauseMapper;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.FICHES) // Aligné dynamiquement sur "/api/v1/fiches"
public class FicheMiseEnCauseController {

    private final FicheMiseEnCauseService ficheService;
    private final FicheMiseEnCauseMapper ficheMapper;

    // Le constructeur explicite résout l'UnsatisfiedDependencyException en liant explicitement le Qualifier
    public FicheMiseEnCauseController(
            @Qualifier("ficheMiseEnCauseServiceImpl") FicheMiseEnCauseService ficheService,
            FicheMiseEnCauseMapper ficheMapper) {
        this.ficheService = ficheService;
        this.ficheMapper = ficheMapper;
    }

    @GetMapping(ApiURLs.FICHES_ID) // URL finale : "/api/v1/fiches/{id}"
    public ResponseEntity<FicheMiseEnCauseResponse> consulter(@PathVariable UUID id) {
        FicheMiseEnCause fiche = ficheService.consulter(id);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    @PutMapping(ApiURLs.FICHES_SOUMETTRE) // URL finale : "/api/v1/fiches/{id}/soumettre"
    public ResponseEntity<FicheMiseEnCauseResponse> soumettre(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.soumettre(id, agentId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    @PutMapping(ApiURLs.FICHES_VALIDER) // URL finale : "/api/v1/fiches/{id}/valider"
    public ResponseEntity<FicheMiseEnCauseResponse> valider(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.valider(id, validateurId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    @PutMapping(ApiURLs.FICHES_REJETER) // URL finale : "/api/v1/fiches/{id}/rejeter"
    public ResponseEntity<FicheMiseEnCauseResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.rejeter(id, motif, validateurId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    @DeleteMapping(ApiURLs.FICHES_ARCHIVER) // URL finale : "/api/v1/fiches/{id}/archiver"
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        ficheService.archiver(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(ApiURLs.FICHES_STATUT_JUDICIAIRE) // URL finale : "/api/v1/fiches/{id}/statut-judiciaire"
    public ResponseEntity<FicheMiseEnCauseResponse> modifierStatutJudiciaire(
            @PathVariable UUID id,
            @RequestBody StatutJudiciaireRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.modifierStatutJudiciaire(id, request, agentId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }
}