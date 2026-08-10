// rapport/service/impl/StatistiqueServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.rapport.service.impl;

import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationFaitRepository;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.rapport.dto.response.StatistiqueGlobaleResponse;
import bf.gov.ascelc.logintegrite_backend.rapport.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueServiceImpl implements StatistiqueService {

    private final PersonneRepository personneRepository;
    private final DossierRepository dossierRepository;
    private final FaitReprocheRepository faitReprocheRepository;
    private final ImplicationFaitRepository implicationFaitRepository;

    @Override
    public StatistiqueGlobaleResponse obtenirStatistiquesGlobales() {
        return StatistiqueGlobaleResponse.builder()
                .totalPersonnesPhysiques(personneRepository.countByTypePersonne(TypePersonne.PHYSIQUE))
                .totalPersonnesMorales(personneRepository.countByTypePersonne(TypePersonne.MORALE))
                .totalDossiersOuverts(dossierRepository.countByStatutDossier(StatutDossier.OUVERT))
                .totalDossiersClotures(dossierRepository.countByStatutDossier(StatutDossier.CLOTURE))
                .parCategorieInfraction(versMap(faitReprocheRepository.compterParCategorieInfraction()))
                .parRegion(versMap(faitReprocheRepository.compterParZone()))
                .parStatutJudiciaire(versMap(implicationFaitRepository.compterParStatutJudiciaire()))
                .build();
    }

    private Map<String, Long> versMap(List<Object[]> lignes) {
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (Object[] ligne : lignes) {
            resultat.put((String) ligne[0], (Long) ligne[1]);
        }
        return resultat;
    }
}
