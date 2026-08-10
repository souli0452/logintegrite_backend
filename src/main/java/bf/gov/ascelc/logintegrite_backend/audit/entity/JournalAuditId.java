// audit/entity/JournalAuditId.java
package bf.gov.ascelc.logintegrite_backend.audit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class JournalAuditId implements Serializable {
    private UUID id;
    private Instant dateAction;

    public JournalAuditId() {}
    public JournalAuditId(UUID id, Instant dateAction) {
        this.id = id;
        this.dateAction = dateAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalAuditId that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(dateAction, that.dateAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateAction);
    }
}
