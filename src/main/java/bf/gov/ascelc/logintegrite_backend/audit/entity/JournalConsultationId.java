// audit/entity/JournalConsultationId.java
package bf.gov.ascelc.logintegrite_backend.audit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class JournalConsultationId implements Serializable {
    private UUID id;
    private Instant dateConsultation;

    public JournalConsultationId() {}
    public JournalConsultationId(UUID id, Instant dateConsultation) {
        this.id = id;
        this.dateConsultation = dateConsultation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalConsultationId that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(dateConsultation, that.dateConsultation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateConsultation);
    }
}
