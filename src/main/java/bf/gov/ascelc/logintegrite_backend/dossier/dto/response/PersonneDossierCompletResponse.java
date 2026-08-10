package bf.gov.ascelc.logintegrite_backend.dossier.dto.response;

import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PersonneDossierCompletResponse {
    private List<ImplicationResponse> implications;
    private List<DossierResponse> dossiers;
    private List<FaitReprocheResponse> faits;
    private List<DocumentResponse> documents;
}
