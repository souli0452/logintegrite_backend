package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.mapper.FicheMiseEnCauseMapper;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import bf.gov.ascelc.logintegrite_backend.service.HistoriqueStatutService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// SOURCE UNIQUE DE VÉRITÉ pour les transitions de statut communes à PP et PM.
// PersonnePhysiqueServiceImpl et PersonneMoraleServiceImpl délèguent ici pour
// soumettre/rejeter/archiver/modifierStatutJudiciaire, qui n'ont aucune règle
// métier différenciée par type de fiche. Seul valider() reste implémenté
// séparément dans chaque sous-service, car il porte une règle spécifique
// (contrôle anti-doublon matricule pour PP, IFU pour PM).
@Service
@RequiredArgsConstructor
public class FicheMiseEnCauseServiceImpl implements FicheMiseEnCauseService {

    private final FicheMiseEnCauseRepository ficheRepo;
    private final FicheMiseEnCauseMapper ficheMapper;
    private final HistoriqueStatutService historiqueStatutService;

    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulter(UUID id) {
        return ficheRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche de mise en cause introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulterAvecDetails(UUID id) {
        FicheMiseEnCause fiche = consulter(id);

        if (fiche.getInfractions() != null) {
            fiche.getInfractions().size();
        }
        if (fiche.getPiecesJointes() != null) {
            fiche.getPiecesJointes().size();
        }
        if (fiche.getHistoriqueStatuts() != null) {
            fiche.getHistoriqueStatuts().size();
        }

        return fiche;
    }

    // MODIFIÉ : délègue désormais à la machine à états de l'entité (garde
    // BROUILLON/REJETE -> EN_ATTENTE_VALIDATION, incrémente nbSoumissions).
    // Avant, ce chemin (utilisé par PUT /api/v1/fiches/{id}/soumettre)
    // contournait totalement la garde métier avec un setStatutFiche brut.
    @Override
    @Transactional
    public FicheMiseEnCause soumettre(UUID id, String agentId) {
        FicheMiseEnCause f = consulter(id);
        f.soumettre(agentId);
        return ficheRepo.save(f);
    }

    // Reste bloqué : la validation porte une règle anti-doublon différente
    // selon le type de fiche (matricule pour PP, IFU pour PM), impossible à
    // exécuter correctement sur le type abstrait FicheMiseEnCause.
    @Override
    @Transactional
    public FicheMiseEnCause valider(UUID id, String validateurId) {
        throw new UnsupportedOperationException(
                "La validation doit être effectuée via PersonnePhysiqueService ou PersonneMoraleService, "
                        + "qui appliquent le contrôle anti-doublon du registre officiel.");
    }

    // MODIFIÉ : délègue à la machine à états (garde EN_ATTENTE_VALIDATION,
    // fixe motifRejet/validateurId/dateValidation de façon cohérente).
    @Override
    @Transactional
    public FicheMiseEnCause rejeter(UUID id, String motif, String validateurId) {
        FicheMiseEnCause f = consulter(id);
        f.rejeter(motif, validateurId);
        return ficheRepo.save(f);
    }

    // MODIFIÉ : délègue à la machine à états (garde ACTIVE/REJETE avant
    // archivage — empêche d'archiver un brouillon jamais validé).
    @Override
    @Transactional
    public FicheMiseEnCause archiver(UUID id) {
        FicheMiseEnCause f = consulter(id);
        f.archiver();
        return ficheRepo.save(f);
    }

    // MODIFIÉ : route désormais systématiquement par HistoriqueStatutService,
    // pour que chaque changement de statut judiciaire (PP comme PM, quel que
    // soit le contrôleur d'entrée) laisse une trace dans historique_statut,
    // comme le prévoit la Phase 1 de la consigne métier.
    @Override
    @Transactional
    public FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        FicheMiseEnCause f = consulter(id);

        HistoriqueStatutRequest histRequest = HistoriqueStatutRequest.builder()
                .ficheId(id)
                .ancientStatut(f.getStatutJudiciaire())
                .nouveauStatut(request.getStatutJudiciaire())
                .motif(request.getMotif())
                .dateJugement(request.getDateJugement())
                .juridiction(request.getJuridiction())
                .typePeine(request.getTypePeine())
                .dureePeine(request.getDureePeine())
                .montantAmende(request.getMontantAmende())
                .motifRelaxe(request.getMotifRelaxe())
                .build();

        historiqueStatutService.create(histRequest);

        return consulter(id);
    }

    @Override
    @Transactional
    public FicheMiseEnCause supprimerBrouillonOuSoumise(UUID id, String userId, boolean isAdmin) {
        FicheMiseEnCause fiche = consulter(id);

        boolean statutSupprimable = "BROUILLON".equals(fiche.getStatutFiche())
                || "EN_ATTENTE_VALIDATION".equals(fiche.getStatutFiche());
        if (!statutSupprimable) {
            throw new IllegalStateException(
                    "Seules les fiches en brouillon ou en attente de validation peuvent être supprimées. "
                            + "Une fiche active doit être archivée par un administrateur.");
        }

        boolean estCreateur = fiche.getCreatedById() != null && fiche.getCreatedById().equals(userId);
        if (!isAdmin && !estCreateur) {
            throw new AccessDeniedException(
                    "Vous ne pouvez supprimer que les fiches (brouillons ou soumises) que vous avez vous-même créées.");
        }

        ficheRepo.delete(fiche);
        return fiche;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMiseEnCauseResponse> rechercherRegistreOfficiel(
            String recherche, UUID regionId, UUID entiteId, Pageable pageable) {
        return ficheRepo.rechercheRegistreOfficiel(regionId, entiteId, recherche, pageable)
                .map(ficheMapper::toResponse);
    }

    @Override
    public long countEnAttente() {
        return ficheRepo.countByStatutFiche("EN_ATTENTE_VALIDATION");
    }

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