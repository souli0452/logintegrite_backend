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
public class PersonneMoralePublicResponse {
    private String denomination;
    private String raisonSociale;
    private String domaineActivite;
    private String statutJudiciaire;
}