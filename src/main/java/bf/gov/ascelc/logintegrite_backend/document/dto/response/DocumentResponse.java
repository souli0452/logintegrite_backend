// document/dto/response/DocumentResponse.java
package bf.gov.ascelc.logintegrite_backend.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

// nomStockage/cheminStockage volontairement absents : details de stockage
// internes, jamais exposes au client - le telechargement passe par un
// endpoint dedie, pas par la lecture directe du chemin.
@Getter
@Builder
@AllArgsConstructor
public class DocumentResponse {
    private UUID id;
    private UUID dossierId;
    private UUID typeDocumentId;
    private String typeDocumentLibelle;
    private String nomOriginal;
    private String typeMime;
    private Long tailleOctets;
    private String hashIntegrite;
    private boolean immuable;
    private UUID uploadeParId;
    private String uploadeParNomComplet;
    private Instant dateUpload;
}
