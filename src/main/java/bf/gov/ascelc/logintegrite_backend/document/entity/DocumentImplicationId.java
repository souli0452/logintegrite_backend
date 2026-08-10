package bf.gov.ascelc.logintegrite_backend.document.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DocumentImplicationId implements Serializable {
    private UUID documentId;
    private UUID implicationId;

    public DocumentImplicationId() {}
    public DocumentImplicationId(UUID documentId, UUID implicationId) {
        this.documentId = documentId;
        this.implicationId = implicationId;
    }

    public UUID getDocumentId() { return documentId; }
    public UUID getImplicationId() { return implicationId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentImplicationId that)) return false;
        return Objects.equals(documentId, that.documentId) && Objects.equals(implicationId, that.implicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, implicationId);
    }
}
