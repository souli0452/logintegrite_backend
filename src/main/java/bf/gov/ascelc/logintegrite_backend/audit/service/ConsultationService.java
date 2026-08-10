// audit/service/ConsultationService.java
package bf.gov.ascelc.logintegrite_backend.audit.service;

import java.util.UUID;

public interface ConsultationService {
    void enregistrer(String entiteConsultee, UUID entiteConsulteeId);
}
