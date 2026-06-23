package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.entity.JournalAudit;
import bf.gov.ascelc.logintegrite_backend.repository.JournalAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs.*;

@RestController
@RequestMapping(AUDIT) // Aligné dynamiquement sur "/api/v1/audit"
@RequiredArgsConstructor
public class AuditController {

    private final JournalAuditRepository auditRepo;

    @GetMapping
    // Remplacement de hasRole par hasAnyAuthority pour correspondre au format exact de tes jetons Keycloak
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Page<JournalAudit>> journal(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditRepo.findAll(
                PageRequest.of(page, size, Sort.by("horodatage").descending())));
    }

    @GetMapping(AUDIT_MES_ACTIONS) // Équivaut à "/api/v1/audit/mes-actions"
    // Remplacement par hasAnyAuthority pour s'assurer qu'un utilisateur possède bien un rôle applicatif minimal
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<Page<JournalAudit>> mesActions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        // Protection contre le NullPointerException en récupérant l'ID utilisateur unique du JWT (sub)
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";

        return ResponseEntity.ok(
                auditRepo.findByUtilisateurId(
                        userId,
                        PageRequest.of(page, size, Sort.by("horodatage").descending())));
    }
}