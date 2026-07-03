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

    @Override
    @Transactional
    public FicheMiseEnCause soumettre(UUID id, String agentId) {
        FicheMiseEnCause f = consulter(id);
        f.soumettre(agentId);
        return ficheRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause valider(UUID id, String validateurId) {
        throw new UnsupportedOperationException(
                "La validation doit être effectuée via PersonnePhysiqueService ou PersonneMoraleService, "
                        + "qui appliquent le contrôle anti-doublon du registre officiel.");
    }

    @Override
    @Transactional
    public FicheMiseEnCause rejeter(UUID id, String motif, String validateurId) {
        FicheMiseEnCause f = consulter(id);
        f.rejeter(motif, validateurId);
        return ficheRepo.save(f);
    }

    @Override
    @Transactional
    public FicheMiseEnCause archiver(UUID id) {
        FicheMiseEnCause f = consulter(id);
        f.archiver();
        return ficheRepo.save(f);
    }

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

    // AJOUT : implémentation — réutilise rechercheGlobale (déjà présente
    // dans le repository, jusque-là inutilisée) filtrée sur EN_ATTENTE_VALIDATION.
    @Override
    @Transactional(readOnly = true)
    public Page<FicheMiseEnCauseResponse> rechercherFileAttenteValidation(UUID regionId, UUID entiteId, Pageable pageable) {
        return ficheRepo.rechercheGlobale(entiteId, regionId, "EN_ATTENTE_VALIDATION", pageable)
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