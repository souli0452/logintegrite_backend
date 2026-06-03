package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
// import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit.ActionAudit;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
// import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fiches/personnes-physiques")
@RequiredArgsConstructor
public class PersonnePhysiqueController {

    private final PersonnePhysiqueService ppService;
    // private final AuditService auditService; // Pour la prochaine itération

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> lister(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        
        StatutFiche sf = statut != null ? StatutFiche.valueOf(statut.toUpperCase()) : null;
        Page<PersonnePhysique> entities = ppService.lister(sf, PageRequest.of(page, size, Sort.by("dateModification").descending()));
        
        return ResponseEntity.ok(entities.map(FicheMiseEnCauseResponse::fromEntity));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> creer(
            @Valid @RequestBody PersonnePhysique pp,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        
        PersonnePhysique saved = ppService.creer(pp);
        // TODO: Intégrer l'audit invisible lors de la saisie
        // auditService.log(jwt, ActionAudit.CREATION, "PersonnePhysique", saved.getId(), "Création PP : " + saved.getNom(), req.getRemoteAddr());
            
        return ResponseEntity.status(HttpStatus.CREATED).body(FicheMiseEnCauseResponse.fromEntity(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PersonnePhysique pp,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        
        PersonnePhysique updated = ppService.modifier(id, pp);
        // TODO: Intégrer l'audit invisible lors de la modification
        // auditService.log(jwt, ActionAudit.MODIFICATION, "PersonnePhysique", id, "Modification PP", req.getRemoteAddr());
            
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(updated));
    }

    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> recherche(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) Long entiteId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<PersonnePhysique> entities = ppService.rechercherPP(nom, entiteId, regionId, statut, PageRequest.of(page, size));
        return ResponseEntity.ok(entities.map(FicheMiseEnCauseResponse::fromEntity));
    }
}
