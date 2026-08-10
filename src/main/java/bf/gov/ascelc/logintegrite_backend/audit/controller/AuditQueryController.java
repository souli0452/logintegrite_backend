package bf.gov.ascelc.logintegrite_backend.audit.controller;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalAuditResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalConsultationResponse;
import bf.gov.ascelc.logintegrite_backend.audit.service.AuditQueryService;
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
}
