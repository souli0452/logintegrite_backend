// rapport/service/StatistiqueService.java
package bf.gov.ascelc.logintegrite_backend.rapport.service;

import bf.gov.ascelc.logintegrite_backend.rapport.dto.response.StatistiqueGlobaleResponse;

public interface StatistiqueService {
    StatistiqueGlobaleResponse obtenirStatistiquesGlobales();
}
