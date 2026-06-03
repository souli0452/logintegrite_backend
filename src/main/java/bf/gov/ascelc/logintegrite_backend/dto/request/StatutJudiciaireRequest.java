package bf.gov.ascelc.logintegrite_backend.dto.request;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutJudiciaire;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StatutJudiciaireRequest {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutJudiciaire nouveauStatut;

    private String motif;
    private LocalDate dateJugement;
    private String juridiction;
    private String typePeine;
    private String dureePeine;
    private BigDecimal montantAmende; // Modifié en BigDecimal pour s'aligner sur l'entité HistoriqueStatut
    private String motifRelaxe;
}
