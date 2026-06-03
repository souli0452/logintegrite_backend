package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
// import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit.ActionAudit;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
// import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
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
@RequestMapping("/api/fiches/personnes-morales")
@RequiredArgsConstructor
public class PersonneMoraleController {

    private final PersonneMoraleService pmService;
    // private final AuditService auditService; // Pour la prochaine itération

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> lister(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        
        StatutFiche sf = statut != null ? StatutFiche.valueOf(statut.toUpperCase()) : null;
        Page<PersonneMorale> entities = pmService.lister(sf, PageRequest.of(page, size, Sort.by("dateModification").descending()));
        
        return ResponseEntity.ok(entities.map(FicheMiseEnCauseResponse::fromEntity));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> creer(
            @Valid @RequestBody PersonneMorale pm,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        
        PersonneMorale saved = pmService.creer(pm);
        // TODO: Intégrer l'audit invisible lors de la saisie
        // auditService.log(jwt, ActionAudit.CREATION, "PersonneMorale", saved.getId(), "Création PM : " + saved.getRaisonSociale(), req.getRemoteAddr());
            
        return ResponseEntity.status(HttpStatus.CREATED).body(FicheMiseEnCauseResponse.fromEntity(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody PersonneMorale pm,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest req) {
        
        PersonneMorale updated = pmService.modifier(id, pm);
        // TODO: Intégrer l'audit invisible lors de la modification
        // auditService.log(jwt, ActionAudit.MODIFICATION, "PersonneMorale", id, "Modification PM", req.getRemoteAddr());
            
        return ResponseEntity.ok(FicheMiseEnCauseResponse.fromEntity(updated));
    }

    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','VALIDATEUR','AGENT')")
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> recherche(
            @RequestParam(required = false) String raisonSociale,
            @RequestParam(required = false) Long entiteId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String typeStructure,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<PersonneMorale> entities = pmService.rechercherPM(raisonSociale, entiteId, regionId, typeStructure, PageRequest.of(page, size));
        return ResponseEntity.ok(entities.map(FicheMiseEnCauseResponse::fromEntity));
    }
}
