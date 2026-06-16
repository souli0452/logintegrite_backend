package bf.gov.ascelc.logintegrite_backend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StatutJudiciaireRequest {
    private String statutJudiciaire; // Renommé pour correspondre à request.getStatutJudiciaire() dans les services
    private String motif;
    private LocalDate dateJugement;
    private String juridiction;
    private String typePeine;
    private Integer dureePeine;
    private Double montantAmende;
    private String motifRelaxe;
}