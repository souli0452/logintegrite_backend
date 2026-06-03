package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.*;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.*;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.*;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonneMoraleServiceImpl implements PersonneMoraleService {

    private final PersonneMoraleRepository pmRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    public PersonneMorale creer(PersonneMorale pm) {
        if (pm.getIfu() != null && pmRepo.existsByIfu(pm.getIfu())) {
            throw new IllegalStateException("IFU déjà utilisé : " + pm.getIfu());
        }
        if (pm.getEntite() != null) {
            EntiteOrganisation e = entiteRepo.findById(pm.getEntite().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Entité introuvable"));
            if (!e.getActif()) {
                throw new IllegalStateException("Entité inactive : " + e.getNom());
            }
            pm.setEntite(e);
        }
        if (pm.getInfractions() != null)
            pm.getInfractions().forEach(i -> i.setFiche(pm));
        if (pm.getPiecesJointes() != null)
            pm.getPiecesJointes().forEach(pj -> pj.setFiche(pm));

        pm.setStatutFiche(StatutFiche.BROUILLON);
        pm.setNbSoumissions(0);
        return pmRepo.save(pm);
    }

    @Override
    public PersonneMorale modifier(Long id, PersonneMorale pm) {
        PersonneMorale existing = pmRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PersonneMorale introuvable : " + id));
        if (pm.getIfu() != null && !pm.getIfu().equals(existing.getIfu()) && pmRepo.existsByIfu(pm.getIfu())) {
            throw new IllegalStateException("IFU déjà utilisé : " + pm.getIfu());
        }
        existing.setRaisonSociale(pm.getRaisonSociale());
        existing.setSigle(pm.getSigle());
        existing.setIfu(pm.getIfu());
        existing.setTypeStructure(pm.getTypeStructure());
        existing.setNomResponsable(pm.getNomResponsable());
        existing.setFonctionResponsable(pm.getFonctionResponsable());
        if (pm.getEntite() != null) existing.setEntite(pm.getEntite());
        if (pm.getRegion() != null) existing.setRegion(pm.getRegion());
        existing.setStatutFiche(StatutFiche.EN_ATTENTE);
        existing.setNbSoumissions(existing.getNbSoumissions() + 1);
        return pmRepo.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonneMorale> lister(StatutFiche statut, Pageable pageable) {
        if (statut != null)
            return pmRepo.findByStatutFiche(statut, pageable);
        return pmRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonneMorale> rechercherPM(String raisonSociale, Long entiteId, Long regionId, String typeStructure, Pageable pageable) {
        return pmRepo.rechercheAvancee(raisonSociale, entiteId, regionId, typeStructure, pageable);
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
