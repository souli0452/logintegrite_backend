package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.config.security.JwtRoleUtils;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.PERSONNES_MORALES)
@RequiredArgsConstructor
public class PersonneMoraleController {

    private final PersonneMoraleService pmService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonneMoraleResponse> creer(@Valid @RequestBody PersonneMoraleRequest request) {
        return ResponseEntity.ok(pmService.creerFiche(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<Page<?>> rechercher(
            @RequestParam(required = false) String raisonSociale,
            @RequestParam(required = false) UUID entiteId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String typeStructure,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraction du rôle via l'utilitaire commun (remplace le bloc dupliqué)
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);

        if (isPublicOnly) {
            return ResponseEntity.ok(pmService.rechercherFichesPublic(raisonSociale, entiteId, regionId, "ACTIVE", typeStructure, pageable));
        }
        return ResponseEntity.ok(pmService.rechercherFichesInterne(raisonSociale, entiteId, regionId, statut, typeStructure, pageable));
    }

    // CORRIGÉ : même correctif que PersonnePhysiqueController.consulter() —
    // bascule vers le DTO public restreint quand l'appelant est ROLE_public
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<?> consulter(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);

        if (isPublicOnly) {
            return ResponseEntity.ok(pmService.obtenirFichePourAffichagePublic(id));
        }
        return ResponseEntity.ok(pmService.obtenirFichePourAffichage(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonneMoraleResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody PersonneMoraleRequest request) {
        return ResponseEntity.ok(pmService.modifierFiche(id, request));
    }

    @PutMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<PersonneMoraleResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.soumettreFiche(id, agentId));
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonneMoraleResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.validerFiche(id, validateurId));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonneMoraleResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.rejeterFiche(id, motif, validateurId));
    }

    @DeleteMapping("/{id}/archiver")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        pmService.archiver(id);
        return ResponseEntity.noContent().build();
    }

    // AJOUT : cet endpoint n'existait pas alors que PersonneMoraleServiceImpl
    // implémente déjà modifierStatutJudiciaireFiche(). PersonnePhysiqueController
    // avait cet endpoint, PersonneMoraleController ne l'avait pas (gap de parité).
    @PutMapping("/{id}/statut-judiciaire")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonneMoraleResponse> modifierStatutJudiciaire(
            @PathVariable UUID id,
            @Valid @RequestBody StatutJudiciaireRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.modifierStatutJudiciaireFiche(id, request, agentId));
    }
}