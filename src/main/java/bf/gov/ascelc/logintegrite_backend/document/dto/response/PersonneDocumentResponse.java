// document/dto/response/PersonneDocumentResponse.java
package bf.gov.ascelc.logintegrite_backend.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

// DTO enrichi pour afficher les documents d'une personne : ajoute
// le contexte du dossier (numero et intitule) au DocumentResponse standard.
@Getter
@Builder
@AllArgsConstructor
public class PersonneDocumentResponse {
    private UUID id;
    private UUID dossierId;
    private String numeroDossier;
    private String intituleDossier;
    private UUID typeDocumentId;
    private String typeDocumentLibelle;
    private String nomOriginal;
    private Long tailleOctets;
    private String typeMime;
    private String hashIntegrite;
    private Instant dateUpload;
}
