package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonnePhysiquePublicResponse {
    private String nom;
    private String prenoms;
    private String nationalite;
    private String fonction;
    private String statutJudiciaire;
}