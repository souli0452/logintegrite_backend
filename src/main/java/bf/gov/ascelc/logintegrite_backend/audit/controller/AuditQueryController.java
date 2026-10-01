package bf.gov.ascelc.logintegrite_backend.audit.controller;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalAuditResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalConsultationResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EvenementConnexionResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.EvenementSecuriteResponse;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditQueryService;
import bf.gov.ascelc.logintegrite_backend.audit.service.JournalSecuriteService;
import bf.gov.ascelc.logintegrite_backend.securite.service.KeycloakAdminService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditQueryController {

    private final AuditQueryService service;
    private final JournalSecuriteService journalSecurite;
    private final KeycloakAdminService keycloak;

    @GetMapping("/journal-audit")
    public Page<JournalAuditResponse> journalAudit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String entiteCible) {
        return service.listerAudit(entiteCible, PageRequest.of(page, size));
    }

    @GetMapping("/journal-consultation")
    public Page<JournalConsultationResponse> journalConsultation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String entiteConsultee) {
        return service.listerConsultations(entiteConsultee, PageRequest.of(page, size));
    }

    @GetMapping("/evenements-poste")
    public Page<EvenementSecuriteResponse> evenementsPoste(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        return journalSecurite.lister(type, PageRequest.of(page, Math.min(size, 100)));
    }

    /** Connexions, deconnexions et echecs de connexion, lus dans Keycloak. type : LOGIN, LOGOUT ou LOGIN_ERROR. */
    @GetMapping("/connexions")
    @SuppressWarnings("unchecked")
    public List<EvenementConnexionResponse> connexions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        int taille = Math.min(size, 100);
        return keycloak.listerEvenements(type, page * taille, taille).stream().map(e -> {
            Map<String, Object> details = e.get("details") instanceof Map<?, ?> d ? (Map<String, Object>) d : Map.of();
            String evenement = String.valueOf(e.get("type"));
            return new EvenementConnexionResponse(
                    Instant.ofEpochMilli(((Number) e.get("time")).longValue()),
                    evenement,
                    String.valueOf(details.getOrDefault("username", e.getOrDefault("userId", "inconnu"))),
                    (String) e.get("ipAddress"),
                    "LOGIN_ERROR".equals(evenement) ? (String) e.get("error") : null,
                    (String) e.get("sessionId"));
        }).toList();
    }
}
