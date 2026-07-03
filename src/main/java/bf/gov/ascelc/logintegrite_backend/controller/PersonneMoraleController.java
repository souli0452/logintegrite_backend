package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.config.security.JwtRoleUtils;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.VerificationExistenceResponse;
import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiURLs.PERSONNES_MORALES)
@RequiredArgsConstructor
public class PersonneMoraleController {

    private final PersonneMoraleService pmService;
    private final AuditService auditService;
    private final HttpServletRequest request;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonneMoraleResponse> creer(@Valid @RequestBody PersonneMoraleRequest req, @AuthenticationPrincipal Jwt jwt) {
        PersonneMoraleResponse response = pmService.creerFiche(req);
        auditService.log(jwt, "CREATION_FICHE_PM", "PersonneMorale", response.getId().toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verifier")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<VerificationExistenceResponse> verifierExistence(
            @RequestParam(required = false) String ifu,
            @RequestParam(required = false) String raisonSociale) {
        return ResponseEntity.ok(pmService.verifierExistence(ifu, raisonSociale));
    }

    @GetMapping("/mes-fiches")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<Page<PersonneMoraleResponse>> mesFiches(
            @RequestParam(required = false) String statut,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(pmService.rechercherMesFiches(userId, statut, pageable));
    }

    // MODIFIÉ : même cloisonnement que PersonnePhysiqueController.rechercher()
    // — voir le commentaire détaillé là-bas.
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
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);
        if (isPublicOnly) {
            return ResponseEntity.ok(pmService.rechercherFichesPublic(raisonSociale, entiteId, regionId, "ACTIVE", typeStructure, pageable));
        }

        boolean isAdmin = JwtRoleUtils.estAdministrateur(jwt);
        if (!isAdmin) {
            boolean isValidateurOnly = JwtRoleUtils.estValidateurUniquement(jwt);
            boolean demandeFileValidation = isValidateurOnly && "EN_ATTENTE_VALIDATION".equals(statut);
            boolean demandeRegistreActif = "ACTIVE".equals(statut);

            if (!demandeFileValidation && !demandeRegistreActif) {
                String userId = jwt.getSubject();
                return ResponseEntity.ok(pmService.rechercherMesFiches(userId, statut, pageable));
            }
        }

        return ResponseEntity.ok(pmService.rechercherFichesInterne(raisonSociale, entiteId, regionId, statut, typeStructure, pageable));
    }

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
            @PathVariable UUID id, @Valid @RequestBody PersonneMoraleRequest req, @AuthenticationPrincipal Jwt jwt) {
        PersonneMoraleResponse response = pmService.modifierFiche(id, req);
        auditService.log(jwt, "MODIFICATION_FICHE_PM", "PersonneMorale", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<PersonneMoraleResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMoraleResponse response = pmService.soumettreFiche(id, agentId);
        auditService.log(jwt, "SOUMISSION_FICHE", "PersonneMorale", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonneMoraleResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMoraleResponse response = pmService.validerFiche(id, validateurId);
        auditService.log(jwt, "VALIDATION_FICHE", "PersonneMorale", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<PersonneMoraleResponse> rejeter(
            @PathVariable UUID id, @RequestParam String motif, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMoraleResponse response = pmService.rejeterFiche(id, motif, validateurId);
        auditService.log(jwt, "REJET_FICHE", "PersonneMorale", id.toString(), motif, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/statut-judiciaire")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<PersonneMoraleResponse> modifierStatutJudiciaire(
            @PathVariable UUID id, @Valid @RequestBody StatutJudiciaireRequest req, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        PersonneMoraleResponse response = pmService.modifierStatutJudiciaireFiche(id, req, agentId);
        auditService.log(jwt, "MODIFICATION_STATUT_JUDICIAIRE", "PersonneMorale", id.toString(),
                "Nouveau statut : " + req.getStatutJudiciaire(), request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/archiver")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> archiver(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        pmService.archiver(id);
        auditService.log(jwt, "ARCHIVAGE_FICHE", "PersonneMorale", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}