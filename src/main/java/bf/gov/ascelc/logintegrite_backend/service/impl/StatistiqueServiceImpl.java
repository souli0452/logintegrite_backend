package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.response.StatistiqueResponse;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService {

    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    public StatistiqueResponse calculer() {
        long total = ficheRepo.count();
        long actives = ficheRepo.findAll().stream().filter(f -> "ACTIVE".equals(f.getStatutFiche())).count();
        long enAttente = ficheRepo.findAll().stream().filter(f -> "EN_ATTENTE_VALIDATION".equals(f.getStatutFiche())).count();

        return StatistiqueResponse.builder()
                .totalFiches(total)
                .fichesEnAttente(enAttente)
                .build();
    }
}