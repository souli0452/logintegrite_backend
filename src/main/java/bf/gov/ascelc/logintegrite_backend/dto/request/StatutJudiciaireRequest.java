package bf.gov.ascelc.logintegrite_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StatutJudiciaireRequest {

    @NotBlank(message = "Le nouveau statut judiciaire est obligatoire")
    private String statutJudiciaire;

    private String motif;
    private LocalDate dateJugement;
    private String juridiction;
    private String typePeine;

    // Integer -> String : c'est la ligne 269 qui plantait dans
    // PersonneMoraleServiceImpl (.dureePeine(request.getDureePeine()))
    private String dureePeine;

    // Double -> BigDecimal, aligné sur HistoriqueStatutRequest
    private BigDecimal montantAmende;

    private String motifRelaxe;
}