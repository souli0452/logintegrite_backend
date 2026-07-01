package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
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

    // ── MÉTHODE LÉGÈRE : Utilisée pour les modifications internes (validation, rejet...)
    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulter(UUID id) {
        return ficheRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche de mise en cause introuvable"));
    }

    // ── NOUVELLE MÉTHODE OPTIMISÉE : Dédiée à la consultation complète pour l'écran Angular
    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulterAvecDetails(UUID id) {
        // 1. On récupère la structure de base de la fiche (rapide)
        FicheMiseEnCause fiche = consulter(id);

        // 2. On force l'initialisation des listes LAZY de manière standard et sécurisée
        if (fiche.getInfractions() != null) {
            fiche.getInfractions().size();
        }
        if (fiche.getPiecesJointes() != null) {
            fiche.getPiecesJointes().size();
        }
        // AJOUT : historiqueStatuts n'était jamais initialisé ici. C'est la cause
        // racine du bug d'écran (le mapper masquait le symptôme avec expression=java(null)
        // au lieu de charger la collection). On l'initialise maintenant comme les deux autres.
        if (fiche.getHistoriqueStatuts() != null) {
            fiche.getHistoriqueStatuts().size();
        }

        return fiche;
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
        f.setStatutJudiciaire(request.getStatutJudiciaire());
        return ficheRepo.save(f);
    }

    @Override
    public long countEnAttente() {
        return ficheRepo.findAll().stream()
                .filter(f -> "EN_ATTENTE_VALIDATION".equals(f.getStatutFiche()))
                .count();
    }

    // MÉTHODES DE RESTITUTION DTO REQUISES PAR L'INTERFACE PARENTE
    // (L'implémentation réelle est déléguée aux sous-services PersonnePhysique / Morale)

    @Override
    public FicheMiseEnCauseResponse obtenirFichePourAffichage(UUID id) {
        throw new UnsupportedOperationException("Cette opération doit être gérée par les implémentations spécifiques (Personne Physique / Morale).");
    }

    @Override
    public FicheMiseEnCauseResponse soumettreFiche(UUID id, String agentId) {
        throw new UnsupportedOperationException("Cette opération doit être gérée par les implémentations spécifiques (Personne Physique / Morale).");
    }

    @Override
    public FicheMiseEnCauseResponse validerFiche(UUID id, String validateurId) {
        throw new UnsupportedOperationException("Cette opération doit être gérée par les implémentations spécifiques (Personne Physique / Morale).");
    }

    @Override
    public FicheMiseEnCauseResponse rejeterFiche(UUID id, String motif, String validateurId) {
        throw new UnsupportedOperationException("Cette opération doit être gérée par les implémentations spécifiques (Personne Physique / Morale).");
    }

    @Override
    public FicheMiseEnCauseResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId) {
        throw new UnsupportedOperationException("Cette opération doit être gérée par les implémentations spécifiques (Personne Physique / Morale).");
    }
}