// rapport/dto/response/StatistiqueGlobaleResponse.java
package bf.gov.ascelc.logintegrite_backend.rapport.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class StatistiqueGlobaleResponse {
    private long totalPersonnesPhysiques;
    private long totalPersonnesMorales;
    private long totalDossiersOuverts;
    private long totalDossiersClotures;
    private Map<String, Long> parCategorieInfraction;
    private Map<String, Long> parRegion;
    private Map<String, Long> parStatutJudiciaire;
}
