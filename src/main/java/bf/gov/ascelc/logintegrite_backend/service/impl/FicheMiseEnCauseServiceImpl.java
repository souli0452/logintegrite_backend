package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.entity.*;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.*;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import org.springframework.context.annotation.Primary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Primary
@RequiredArgsConstructor
@Transactional
public class FicheMiseEnCauseServiceImpl implements FicheMiseEnCauseService {

    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulter(Long id) {
       return ficheRepo.findByIdWithRelations(id).orElseThrow(() ->
        new ResourceNotFoundException("Fiche introuvable : " + id));
    }

    @Override
    public FicheMiseEnCause soumettre(Long id, String agentId) {
        FicheMiseEnCause f = consulter(id);
        if (f.getStatutFiche() != StatutFiche.BROUILLON && f.getStatutFiche() != StatutFiche.REJETEE) {
            throw new IllegalStateException("Seule une fiche BROUILLON ou REJETEE peut être soumise.");
        }
        
        // Vérification : seul le créateur peut soumettre
        if (!agentId.equals(f.getCreateurId())) {
            throw new IllegalStateException("Seul le créateur peut soumettre cette fiche.");
        }
        
        f.setStatutFiche(StatutFiche.EN_ATTENTE);
        f.setNbSoumissions(f.getNbSoumissions() + 1);
        return ficheRepo.save(f);
    }

    @Override
    public FicheMiseEnCause valider(Long id, String validateurId) {
        FicheMiseEnCause f = consulter(id);
        if (f.getStatutFiche() != StatutFiche.EN_ATTENTE) {
            throw new IllegalStateException("Seule une fiche EN_ATTENTE peut être validée.");
        }
        
        f.setStatutFiche(StatutFiche.ACTIVE);
        f.setValidateurId(validateurId);
        f.setDateValidation(LocalDateTime.now());
        return ficheRepo.save(f);
    }

    @Override
    public FicheMiseEnCause rejeter(Long id, String motif, String validateurId) {
        if (motif == null || motif.trim().length() < 20) {
            throw new IllegalStateException("Le motif doit comporter au moins 20 caractères.");
        }
        
        FicheMiseEnCause f = consulter(id);
        if (f.getStatutFiche() != StatutFiche.EN_ATTENTE) {
            throw new IllegalStateException("Seule une fiche EN_ATTENTE peut être rejetée.");
        }
        
        f.setStatutFiche(StatutFiche.REJETEE);
        f.setMotifRejet(motif.trim());
        f.setValidateurId(validateurId);
        return ficheRepo.save(f);
    }

    @Override
    public FicheMiseEnCause archiver(Long id) {
        FicheMiseEnCause f = consulter(id);
        f.setStatutFiche(StatutFiche.ARCHIVEE);
        return ficheRepo.save(f);
    }

    @Override
    public FicheMiseEnCause modifierStatutJudiciaire(Long id, StatutJudiciaireRequest request, String agentId) {
        FicheMiseEnCause f = consulter(id);

        // Construction de l'historique complet basé sur ton DTO et ton entité HistoriqueStatut
        HistoriqueStatut historique = HistoriqueStatut.builder()
            .fiche(f)
            .ancienStatut(f.getStatutJudiciaire())
            .nouveauStatut(request.getNouveauStatut())
            .motif(request.getMotif())
            .dateJugement(request.getDateJugement())
            .juridiction(request.getJuridiction())
            .typePeine(request.getTypePeine())
            .dureePeine(request.getDureePeine())
            .montantAmende(request.getMontantAmende())
            .motifRelaxe(request.getMotifRelaxe())
            .agentId(agentId)
            .build();

        // Ajout à la collection (la cascade Hibernate s'occupera de la sauvegarde automatique)
        f.getHistoriqueStatuts().add(historique);
        
        // Mise à jour de l'état de la fiche principale
        f.setStatutJudiciaire(request.getNouveauStatut());
        f.setStatutFiche(StatutFiche.EN_ATTENTE); // Repasse en validation pour l'ASCE-LC
        
        return ficheRepo.save(f);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnAttente() {
        return ficheRepo.countByStatutFiche(StatutFiche.EN_ATTENTE);
    }
}
