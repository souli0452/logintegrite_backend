package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FicheMiseEnCauseServiceImpl implements FicheMiseEnCauseService {

    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulter(UUID id) {
        return ficheRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche de mise en cause introuvable"));
    }

    @Override
    @Transactional
    public FicheMiseEnCause soumettre(UUID id, String agentId) {
        FicheMiseEnCause f = consulter(id);
        f.setStatutFiche("EN_ATTENTE_VALIDATION");
        return ficheRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause valider(UUID id, String validateurId) {
        FicheMiseEnCause f = consulter(id);
        f.setStatutFiche("ACTIVE");
        return ficheRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause rejeter(UUID id, String motif, String validateurId) {
        FicheMiseEnCause f = consulter(id);
        f.setStatutFiche("REJETE");
        return ficheRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause archiver(UUID id) {
        FicheMiseEnCause f = consulter(id);
        f.setStatutFiche("ARCHIVE");
        return ficheRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        FicheMiseEnCause f = consulter(id);
        // Utilisation du getter exact présent dans le DTO corrigé
        f.setStatutJudiciaire(request.getStatutJudiciaire());
        return ficheRepo.save(f);
    }

    @Override
    public long countEnAttente() {
        return ficheRepo.findAll().stream()
                .filter(f -> "EN_ATTENTE_VALIDATION".equals(f.getStatutFiche()))
                .count();
    }
}