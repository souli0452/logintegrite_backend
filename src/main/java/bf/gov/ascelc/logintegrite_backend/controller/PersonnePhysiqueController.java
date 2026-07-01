package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.config.security.JwtRoleUtils;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
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
@RequestMapping(ApiURLs.PERSONNES_PHYSIQUES) // Ou ton URL configurée
@RequiredArgsConstructor
public class PersonnePhysiqueController {

    private final PersonnePhysiqueService ppService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonnePhysiqueResponse> creer(@Valid @RequestBody PersonnePhysiqueRequest request) {
        return ResponseEntity.ok(ppService.creerFiche(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<Page<?>> rechercher(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) UUID entiteId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String statut,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraction du rôle via l'utilitaire commun (remplace le bloc dupliqué)
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);

        if (isPublicOnly) {
            return ResponseEntity.ok(ppService.rechercherFichesPublic(nom, entiteId, regionId, "ACTIVE", pageable));
        }
        return ResponseEntity.ok(ppService.rechercherFichesInterne(nom, entiteId, regionId, statut, pageable));
    }

    // CORRIGÉ : avant, ce endpoint renvoyait toujours le DTO complet (matricule,
    // date de naissance, nationalité...) même pour un appelant ROLE_public.
    // On bascule maintenant sur le DTO restreint pour le public, comme rechercher().
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<?> consulter(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);

        if (isPublicOnly) {
            return ResponseEntity.ok(ppService.obtenirFichePourAffichagePublic(id));
        }
        return ResponseEntity.ok(ppService.obtenirFichePourAffichage(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonnePhysiqueResponse> modifier(
            @PathVariable UUID id,
            @Valid @RequestBody PersonnePhysiqueRequest request) {
        return ResponseEntity.ok(ppService.modifierFiche(id, request));
    }

    @PutMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<PersonnePhysiqueResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.soumettreFiche(id, agentId));
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonnePhysiqueResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.validerFiche(id, validateurId));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonnePhysiqueResponse> rejeter(
            @PathVariable UUID id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.rejeterFiche(id, motif, validateurId));
    }

    @PutMapping("/{id}/statut-judiciaire")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonnePhysiqueResponse> modifierStatutJudiciaire(
            @PathVariable UUID id,
            @Valid @RequestBody StatutJudiciaireRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.modifierStatutJudiciaireFiche(id, request, agentId));
    }

    @DeleteMapping("/{id}/archiver")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> archiver(@PathVariable UUID id) {
        ppService.archiver(id);
        return ResponseEntity.noContent().build();
    }
}
