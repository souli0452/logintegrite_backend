package bf.gov.ascelc.logintegrite_backend.audit.service;

import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalAuditResponse;
import bf.gov.ascelc.logintegrite_backend.audit.dto.response.JournalConsultationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditQueryService {
    Page<JournalAuditResponse> listerAudit(String entiteCible, Pageable pageable);
    Page<JournalConsultationResponse> listerConsultations(String entiteConsultee, Pageable pageable);
}
