package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.config.security.JwtRoleUtils;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.VerificationExistenceResponse;
import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.PERSONNES_PHYSIQUES)
@RequiredArgsConstructor
public class PersonnePhysiqueController {

    private final PersonnePhysiqueService ppService;
    private final AuditService auditService;
    private final HttpServletRequest request;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonnePhysiqueResponse> creer(@Valid @RequestBody PersonnePhysiqueRequest req, @AuthenticationPrincipal Jwt jwt) {
        PersonnePhysiqueResponse response = ppService.creerFiche(req);
        auditService.log(jwt, "CREATION_FICHE_PP", "PersonnePhysique", response.getId().toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verifier")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<VerificationExistenceResponse> verifierExistence(
            @RequestParam(required = false) String matricule,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenoms,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance) {
        return ResponseEntity.ok(ppService.verifierExistence(matricule, nom, prenoms, dateNaissance));
    }

    @GetMapping("/mes-fiches")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<Page<PersonnePhysiqueResponse>> mesFiches(
            @RequestParam(required = false) String statut,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(ppService.rechercherMesFiches(userId, statut, pageable));
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
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);
        if (isPublicOnly) {
            return ResponseEntity.ok(ppService.rechercherFichesPublic(nom, entiteId, regionId, "ACTIVE", pageable));
        }
        return ResponseEntity.ok(ppService.rechercherFichesInterne(nom, entiteId, regionId, statut, pageable));
    }

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
            @PathVariable UUID id, @Valid @RequestBody PersonnePhysiqueRequest req, @AuthenticationPrincipal Jwt jwt) {
        PersonnePhysiqueResponse response = ppService.modifierFiche(id, req);
        auditService.log(jwt, "MODIFICATION_FICHE_PP", "PersonnePhysique", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<PersonnePhysiqueResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysiqueResponse response = ppService.soumettreFiche(id, agentId);
        auditService.log(jwt, "SOUMISSION_FICHE", "PersonnePhysique", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonnePhysiqueResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysiqueResponse response = ppService.validerFiche(id, validateurId);
        auditService.log(jwt, "VALIDATION_FICHE", "PersonnePhysique", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonnePhysiqueResponse> rejeter(
            @PathVariable UUID id, @RequestParam String motif, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysiqueResponse response = ppService.rejeterFiche(id, motif, validateurId);
        auditService.log(jwt, "REJET_FICHE", "PersonnePhysique", id.toString(), motif, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/statut-judiciaire")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonnePhysiqueResponse> modifierStatutJudiciaire(
            @PathVariable UUID id, @Valid @RequestBody StatutJudiciaireRequest req, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonnePhysiqueResponse response = ppService.modifierStatutJudiciaireFiche(id, req, agentId);
        auditService.log(jwt, "MODIFICATION_STATUT_JUDICIAIRE", "PersonnePhysique", id.toString(),
                "Nouveau statut : " + req.getStatutJudiciaire(), request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/archiver")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> archiver(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        ppService.archiver(id);
        auditService.log(jwt, "ARCHIVAGE_FICHE", "PersonnePhysique", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}