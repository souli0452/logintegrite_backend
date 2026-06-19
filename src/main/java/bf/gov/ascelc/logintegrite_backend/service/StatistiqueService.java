package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatistiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatistiqueResponse;

public interface StatistiqueService {
    StatistiqueResponse calculer(StatistiqueRequest filtres);
}