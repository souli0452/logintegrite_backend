// audit/service/AuditService.java
package bf.gov.ascelc.logintegrite_backend.audit.service;

import java.util.Map;
import java.util.UUID;

public interface AuditService {
    void enregistrer(String action, String entiteCible, UUID entiteCibleId,
                      Map<String, Object> valeurAvant, Map<String, Object> valeurApres);
}
