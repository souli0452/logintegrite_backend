package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonneMoraleServiceImpl implements PersonneMoraleService {

    private final PersonneMoraleRepository pmRepo;

    @Override
    @Transactional
    public PersonneMorale creer(PersonneMorale pm) {
        pm.setStatutFiche("BROUILLON");
        return pmRepo.save(pm);
    }

    @Override
    @Transactional
    public PersonneMorale modifier(UUID id, PersonneMorale pm) {
        PersonneMorale existant = pmRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Personne morale introuvable"));

        existant.setRaisonSociale(pm.getRaisonSociale());
        existant.setSigle(pm.getSigle());
        existant.setIfu(pm.getIfu());
        existant.setEntite(pm.getEntite());
        existant.setRegion(pm.getRegion());

        return pmRepo.save(existant);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonneMorale consulter(UUID id) {
        return pmRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche introuvable"));
    }

    @Override
    @Transactional
    public FicheMiseEnCause soumettre(UUID id, String agentId) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("EN_ATTENTE_VALIDATION");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause valider(UUID id, String validateurId) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("ACTIVE");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause rejeter(UUID id, String motif, String validateurId) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("REJETE");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause archiver(UUID id) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("ARCHIVE");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        PersonneMorale f = consulter(id);
        // Correction ici pour correspondre au getter du DTO révisé
        f.setStatutJudiciaire(request.getStatutJudiciaire());
        return pmRepo.save(f);
    }

    @Override
    public long countEnAttente() {
        return pmRepo.findAll().stream()
                .filter(f -> "EN_ATTENTE_VALIDATION".equals(f.getStatutFiche()))
                .count();
    }
}