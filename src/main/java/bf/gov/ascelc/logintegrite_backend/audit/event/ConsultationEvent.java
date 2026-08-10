// audit/event/ConsultationEvent.java
package bf.gov.ascelc.logintegrite_backend.audit.event;

import java.util.UUID;

public record ConsultationEvent(String entiteConsultee, UUID entiteConsulteeId, UUID utilisateurId) {
}
