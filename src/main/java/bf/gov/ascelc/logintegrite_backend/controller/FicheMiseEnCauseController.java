package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
// import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit.ActionAudit;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
// import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fiches")
@RequiredArgsConstructor
public class FicheMiseEnCauseController {

    private final FicheMiseEnCauseService ficheService;
    private final FicheMiseEnCauseRepository ficheRepo;
    // private final AuditService auditService; // Pour la prochaine itération

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> consulter(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        FicheMiseEnCause f = ficheService.consulter(id);
        // TODO: Intégrer l'audit invisible exigé à l'Itération 3
        // auditService.log(jwt, ActionAudit.CONSULTATION, "FicheMiseEnCause", id, "Consultation", req.getRemoteAddr());
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(f));
    }

    @PutMapping("/{id}/soumettre")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> soumettre(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        FicheMiseEnCause f = ficheService.soumettre(id, jwt.getSubject());
        // TODO: Intégrer l'audit invisible exigé à l'Itération 3
        // auditService.log(jwt, ActionAudit.MODIFICATION, "FicheMiseEnCause", id, "Soumission", req.getRemoteAddr());
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(f));
    }

    @PutMapping("/{id}/valider")
    @PreAuthorize("hasAnyRole('VALIDATEUR','ADMINISTRATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> valider(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        FicheMiseEnCause f = ficheService.valider(id, jwt.getSubject());
        // TODO: Intégrer l'audit invisible exigé à l'Itération 3
        // auditService.log(jwt, ActionAudit.VALIDATION, "FicheMiseEnCause", id, "Validation", req.getRemoteAddr());
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(f));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyRole('VALIDATEUR','ADMINISTRATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> rejeter(
            @PathVariable Long id,
            @RequestParam String motif,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        FicheMiseEnCause f = ficheService.rejeter(id, motif, jwt.getSubject());
        // TODO: Intégrer l'audit invisible exigé à l'Itération 3
        // auditService.log(jwt, ActionAudit.REJET, "FicheMiseEnCause", id, "Rejet: " + motif, req.getRemoteAddr());
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(f));
    }

    @PutMapping("/{id}/archiver")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> archiver(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        FicheMiseEnCause f = ficheService.archiver(id);
        // TODO: Intégrer l'audit invisible exigé à l'Itération 3
        // auditService.log(jwt, ActionAudit.ARCHIVAGE, "FicheMiseEnCause", id, "Archivage", req.getRemoteAddr());
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(f));
    }

    /** Modification complète du statut judiciaire d'une cible avec historique complet */
    @PutMapping("/{id}/statut-judiciaire")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> modifierStatutJudiciaire(
            @PathVariable Long id,
            @Valid @RequestBody StatutJudiciaireRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        
        FicheMiseEnCause f = ficheService.modifierStatutJudiciaire(
            id, request, jwt.getSubject()
        );
        
        // TODO: Intégrer l'audit invisible exigé à l'Itération 3
        // auditService.log(jwt, ActionAudit.MODIFICATION, "FicheMiseEnCause", id, "Changement Statut Judiciaire: " + request.getNouveauStatut(), req.getRemoteAddr());
            
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(f));
    }

    /** Recherche globale paginée et transformée en DTO pour le Frontend */
    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> rechercheGlobale(
            @RequestParam(required = false) Long entiteId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<FicheMiseEnCause> entities = ficheRepo.rechercheGlobale(
            entiteId, regionId, statut,
            PageRequest.of(page, size, Sort.by("dateModification").descending())
        );
        
        Page<FicheMiseEnCauseResponse> dtoPage = entities.map(FicheMiseEnCauseResponse::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
}
