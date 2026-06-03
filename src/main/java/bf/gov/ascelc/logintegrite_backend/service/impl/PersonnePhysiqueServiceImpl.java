package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.*;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.*;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.*;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonnePhysiqueServiceImpl implements PersonnePhysiqueService {

    private final PersonnePhysiqueRepository ppRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    public PersonnePhysique creer(PersonnePhysique pp) {
        if (pp.getMatricule() != null && ppRepo.existsByMatricule(pp.getMatricule())) {
            throw new IllegalStateException("Matricule déjà utilisé : " + pp.getMatricule());
        }
        if (pp.getEntite() != null) {
            EntiteOrganisation e = entiteRepo.findById(pp.getEntite().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Entité introuvable"));
            if (!e.getActif()) {
                throw new IllegalStateException("Entité inactive. Utilisez l'entité active correspondante : " + e.getNom());
            }
            pp.setEntite(e);
        }
        if (pp.getInfractions() != null) {
            pp.getInfractions().forEach(i -> i.setFiche(pp));
        }
        if (pp.getPiecesJointes() != null) {
            pp.getPiecesJointes().forEach(pj -> pj.setFiche(pp));
        }

        pp.setStatutFiche(StatutFiche.BROUILLON);
        pp.setNbSoumissions(0);
        return ppRepo.save(pp);
    }

    @Override
    public PersonnePhysique modifier(Long id, PersonnePhysique pp) {
        PersonnePhysique existing = ppRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PersonnePhysique introuvable : " + id));
        if (pp.getMatricule() != null && !pp.getMatricule().equals(existing.getMatricule()) && ppRepo.existsByMatricule(pp.getMatricule())) {
            throw new IllegalStateException("Matricule déjà utilisé : " + pp.getMatricule());
        }
        existing.setNom(pp.getNom());
        existing.setPrenoms(pp.getPrenoms());
        existing.setDateNaissance(pp.getDateNaissance());
        existing.setLieuNaissance(pp.getLieuNaissance());
        existing.setNationalite(pp.getNationalite());
        existing.setMatricule(pp.getMatricule());
        existing.setFonction(pp.getFonction());
        if (pp.getEntite() != null) existing.setEntite(pp.getEntite());
        if (pp.getRegion() != null) existing.setRegion(pp.getRegion());

        existing.setStatutFiche(StatutFiche.EN_ATTENTE);
        existing.setNbSoumissions(existing.getNbSoumissions() + 1);
        return ppRepo.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonnePhysique> lister(StatutFiche statut, Pageable pageable) {
        if (statut != null)
            return ppRepo.findByStatutFiche(statut, pageable);
        return ppRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonnePhysique> rechercherPP(String nom, Long entiteId, Long regionId, String statut, Pageable pageable) {
        return ppRepo.rechercheAvancee(nom, entiteId, regionId, statut, pageable);
    }

    // ── MÉTHODES DE CONTRAT OBLIGATOIRES (DÉLÉGATION) ──
    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulter(Long id) {
        return ficheRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fiche introuvable : " + id));
    }

    @Override public FicheMiseEnCause soumettre(Long id, String agentId) { throw new UnsupportedOperationException("Passer par FicheMiseEnCauseService"); }
    @Override public FicheMiseEnCause valider(Long id, String validateurId) { throw new UnsupportedOperationException("Passer par FicheMiseEnCauseService"); }
    @Override public FicheMiseEnCause rejeter(Long id, String motif, String validateurId) { throw new UnsupportedOperationException("Passer par FicheMiseEnCauseService"); }
    @Override public FicheMiseEnCause archiver(Long id) { throw new UnsupportedOperationException("Passer par FicheMiseEnCauseService"); }
    @Override public FicheMiseEnCause modifierStatutJudiciaire(Long id, bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest req, String agentId) { throw new UnsupportedOperationException("Passer par FicheMiseEnCauseService"); }

    @Override
    @Transactional(readOnly = true)
    public long countEnAttente() {
        return ficheRepo.countByStatutFiche(StatutFiche.EN_ATTENTE);
    }
}
