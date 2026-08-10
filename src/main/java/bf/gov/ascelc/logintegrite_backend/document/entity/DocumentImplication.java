// document/entity/DocumentImplication.java
package bf.gov.ascelc.logintegrite_backend.document.entity;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "document_implication", schema = "documents")
@Getter
@Setter
public class DocumentImplication {

    @EmbeddedId
    private DocumentImplicationId id = new DocumentImplicationId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentId")
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("implicationId")
    @JoinColumn(name = "implication_id")
    private Implication implication;

    @Column(name = "date_creation", insertable = false, updatable = false)
    private Instant dateCreation;
}
