package bf.gov.ascelc.logintegrite_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class StatistiqueResponse {
    private long totalFiches;
    private long fichesActives;
    private long fichesEnAttente;
    private long fichesBrouillon;
    private long fichesRejetees;

    private Map<String, Long> parTypeFiche;

    private String groupBy;
    private Map<String, Long> repartitionParametrable;

    private Map<String, Long> parStatutJudiciaire;
    private Map<String, Long> parNatureInfraction;
    private Map<String, Long> top5Entites;
}