package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class StatistiqueResponse {

    private long totalFiches; // Aligné pour éviter le conflit avec .totalFiches(total)
    private long fichesEnAttente;
    private long fichesBrouillon;
    private Map<String, Long> parStatutJudiciaire;
    private Map<String, Long> parNatureInfraction;
    private Map<String, Long> top5Entites;
}