// document/dto/request/DocumentRequest.java
package bf.gov.ascelc.logintegrite_backend.document.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
public class DocumentRequest {

    @NotNull(message = "Le fichier est obligatoire")
    private MultipartFile fichier;

    @NotNull(message = "Le type de document est obligatoire")
    private UUID typeDocumentId;
}
