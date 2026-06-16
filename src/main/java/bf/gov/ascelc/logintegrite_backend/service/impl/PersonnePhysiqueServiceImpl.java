package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonnePhysiqueServiceImpl implements PersonnePhysiqueService {

    private final PersonnePhysiqueRepository ppRepo;

    @Override
    @Transactional
    public PersonnePhysique creer(PersonnePhysique pp) {
        pp.setStatutFiche("BROUILLON");
        return ppRepo.save(pp);
    }

    @Override
    @Transactional
    public PersonnePhysique modifier(UUID id, PersonnePhysique pp) {
        PersonnePhysique existant = ppRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Personne physique introuvable"));

        existant.setNom(pp.getNom());
        existant.setPrenoms(pp.getPrenoms());
        existant.setMatricule(pp.getMatricule());
        existant.setEntite(pp.getEntite());
        existant.setRegion(pp.getRegion());

        return ppRepo.save(existant);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonnePhysique consulter(UUID id) {
        return ppRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche introuvable"));
    }

    @Override
    @Transactional
    public FicheMiseEnCause soumettre(UUID id, String agentId) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("EN_ATTENTE_VALIDATION");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause valider(UUID id, String validateurId) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("ACTIVE");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause rejeter(UUID id, String motif, String validateurId) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("REJETE");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause archiver(UUID id) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("ARCHIVE");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        PersonnePhysique f = consulter(id);
        // Modification effectuée ici pour utiliser le bon getter
        f.setStatutJudiciaire(request.getStatutJudiciaire());
        return ppRepo.save(f);
    }

    @Override
    public long countEnAttente() {
        return ppRepo.findAll().stream()
                .filter(f -> "EN_ATTENTE_VALIDATION".equals(f.getStatutFiche()))
                .count();
    }
}