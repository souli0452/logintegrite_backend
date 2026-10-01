package bf.gov.ascelc.logintegrite_backend.audit.controller;

import bf.gov.ascelc.logintegrite_backend.audit.dto.request.EvenementPosteRequest;
import bf.gov.ascelc.logintegrite_backend.audit.service.JournalSecuriteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Reception des evenements de securite releves sur le poste (tout utilisateur connecte). */
@RestController
@RequestMapping("/api/v1/audit/evenements-poste")
@RequiredArgsConstructor
public class AuditPosteController {

    private final JournalSecuriteService service;

    /** Tout role applicatif peut signaler un evenement de son propre poste (jamais celui d'un autre). */
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','VALIDATEUR','CONSULTANT')")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enregistrer(@Valid @RequestBody EvenementPosteRequest requete, HttpServletRequest http) {
        service.enregistrer(requete, http.getHeader(HttpHeaders.USER_AGENT));
    }
}
