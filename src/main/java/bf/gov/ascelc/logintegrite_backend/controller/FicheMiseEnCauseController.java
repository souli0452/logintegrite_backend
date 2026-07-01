package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.config.security.JwtRoleUtils;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheDetailResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.FicheMiseEnCauseMapper;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiURLs.FICHES) // Aligné dynamiquement sur "/api/v1/fiches"
public class FicheMiseEnCauseController {

    private final FicheMiseEnCauseService ficheService;
    private final FicheMiseEnCauseMapper ficheMapper;

    // Injections des deux services pour fusionner les fiches sans modifier FicheMiseEnCauseService
    private final PersonnePhysiqueService ppService;
    private final PersonneMoraleService pmService;

    public FicheMiseEnCauseController(
            @Qualifier("ficheMiseEnCauseServiceImpl") FicheMiseEnCauseService ficheService,
            FicheMiseEnCauseMapper ficheMapper,
            PersonnePhysiqueService ppService,
            PersonneMoraleService pmService) {
        this.ficheService = ficheService;
        this.ficheMapper = ficheMapper;
        this.ppService = ppService;
        this.pmService = pmService;
    }

    @GetMapping // Intercepte le "GET /api/v1/fiches" pour le Dashboard Angular
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<?> lister(@AuthenticationPrincipal Jwt jwt) {

        // Extraction du rôle via l'utilitaire commun (remplace le bloc dupliqué dans PP/PM)
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);

        if (isPublicOnly) {
            // Évite d'appeler 'toPublicResponseList' qui n'existe pas dans le Mapper
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Fusion dynamique des listes PP et PM pour l'Option B
        List<FicheMiseEnCauseResponse> responses = new ArrayList<>();

        responses.addAll(ppService.listerTout().stream()
                .map(ficheMapper::toResponse)
                .collect(Collectors.toList()));

        responses.addAll(pmService.listerTout().stream()
                .map(ficheMapper::toResponse)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(responses);
    }

    // AJOUT : cet écran affiche les données complètes (comité de validation),
    // pas de rôle public ici, contrairement à lister()
    @GetMapping(ApiURLs.FICHES_ID) // URL finale : "/api/v1/fiches/{id}"
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<FicheDetailResponse> consulter(@PathVariable UUID id) {
        FicheMiseEnCause fiche = ficheService.consulterAvecDetails(id);
        return ResponseEntity.ok(ficheMapper.toDetailResponse(fiche));
    }

    // AJOUT : aucune protection avant, accessible à tout utilisateur authentifié
    @PutMapping(ApiURLs.FICHES_SOUMETTRE) // URL finale : "/api/v1/fiches/{id}/soumettre"
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> soumettre(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.soumettre(id, agentId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    // AJOUT : aucune protection avant, action métier sensible réservée au validateur
    @PutMapping(ApiURLs.FICHES_VALIDER) // URL finale : "/api/v1/fiches/{id}/valider"
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> valider(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.valider(id, validateurId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    // AJOUT : aucune protection avant
    @PutMapping(ApiURLs.FICHES_REJETER) // URL finale : "/api/v1/fiches/{id}/rejeter"
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.rejeter(id, motif, validateurId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }

    // AJOUT : aucune protection avant
    @DeleteMapping(ApiURLs.FICHES_ARCHIVER) // URL finale : "/api/v1/fiches/{id}/archiver"
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        ficheService.archiver(id);
        return ResponseEntity.noContent().build();
    }

    // AJOUT : aucune protection avant
    @PutMapping(ApiURLs.FICHES_STATUT_JUDICIAIRE) // URL finale : "/api/v1/fiches/{id}/statut-judiciaire"
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> modifierStatutJudiciaire(
            @PathVariable UUID id,
            @RequestBody StatutJudiciaireRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCause fiche = ficheService.modifierStatutJudiciaire(id, request, agentId);
        return ResponseEntity.ok(ficheMapper.toResponse(fiche));
    }
}
