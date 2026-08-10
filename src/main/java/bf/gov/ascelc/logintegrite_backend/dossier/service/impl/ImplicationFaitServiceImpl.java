// dossier/service/impl/ImplicationFaitServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationFaitRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.MiseAJourStatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.ImplicationFaitMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationFaitRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.ImplicationFaitService;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.StatutJudiciaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImplicationFaitServiceImpl implements ImplicationFaitService {

    private final ImplicationFaitRepository repository;
    private final ImplicationRepository implicationRepository;
    private final FaitReprocheRepository faitReprocheRepository;
    private final StatutJudiciaireRepository statutJudiciaireRepository;
    private final ImplicationFaitMapper mapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<ImplicationFaitResponse> listerParImplication(UUID implicationId) {
        return repository.findByImplicationId(implicationId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public ImplicationFaitResponse creer(UUID implicationId, ImplicationFaitRequest request) {
        Implication implication = implicationRepository.findById(implicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Implication", implicationId));
        FaitReproche fait = faitReprocheRepository.findById(request.getFaitReprocheId())
                .orElseThrow(() -> new ResourceNotFoundException("Fait reproche", request.getFaitReprocheId()));

        ImplicationFait entite = new ImplicationFait();
        entite.setImplication(implication);
        entite.setFaitReproche(fait);
        entite.setStatutJudiciaire(statutJudiciaireRepository.findById(request.getStatutJudiciaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire", request.getStatutJudiciaireId())));
        entite.setDateStatut(LocalDate.now());
        entite.setCommentaire(request.getCommentaire());
        ImplicationFait sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "ImplicationFait", sauvegarde.getId(), null,
                Map.of("implicationId", implication.getId().toString(),
                       "faitReprocheId", fait.getId().toString(),
                       "statutJudiciaire", sauvegarde.getStatutJudiciaire().getLibelle()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public ImplicationFaitResponse mettreAJourStatut(UUID implicationFaitId, MiseAJourStatutJudiciaireRequest request) {
        ImplicationFait entite = repository.findById(implicationFaitId)
                .orElseThrow(() -> new ResourceNotFoundException("Lien implication-fait", implicationFaitId));
        Map<String, Object> avant = Map.of("statutJudiciaire",
                entite.getStatutJudiciaire() != null ? entite.getStatutJudiciaire().getLibelle() : "aucun");

        // 1. Mise à jour du statut sur la liaison
        var nouveauStatut = statutJudiciaireRepository.findById(request.getStatutJudiciaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire", request.getStatutJudiciaireId()));

        entite.setStatutJudiciaire(nouveauStatut);
        entite.setDateStatut(request.getDateStatut() != null ? request.getDateStatut() : LocalDate.now());
        if (request.getMotif() != null) {
            entite.setCommentaire(request.getMotif());
        }
        ImplicationFait sauvegarde = repository.save(entite);

        // 2. Propagation sur l'implication parente : elle reflète le dernier statut modifié.
        //    Sans cette propagation, ImplicationResponse (utilisé partout dans l'UI) reste
        //    figé sur l'ancien statut alors que la liaison a bien changé.
        Implication implicationParente = entite.getImplication();
        implicationParente.setStatutJudiciaire(nouveauStatut);
        if (request.getAutoriteCompetente() != null) {
            implicationParente.setAutoriteCompetente(request.getAutoriteCompetente());
        }
        if (request.getReferenceAffaire() != null) {
            implicationParente.setReferenceAffaire(request.getReferenceAffaire());
        }
        implicationRepository.save(implicationParente);

        // 3. Audit — traçabilité obligatoire de tout changement de statut judiciaire
        auditService.enregistrer("MODIFICATION_STATUT_JUDICIAIRE", "ImplicationFait", sauvegarde.getId(), avant,
                Map.of("statutJudiciaire", sauvegarde.getStatutJudiciaire().getLibelle(),
                       "autoriteCompetente", request.getAutoriteCompetente() != null ? request.getAutoriteCompetente() : "—",
                       "referenceAffaire", request.getReferenceAffaire() != null ? request.getReferenceAffaire() : "—"));

        return mapper.toResponse(sauvegarde);
    }
}
