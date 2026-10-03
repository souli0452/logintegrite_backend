// dossier/service/impl/ImplicationServiceImpl.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.ImplicationMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.ImplicationService;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.RoleImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.StatutJudiciaireRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.RegleDossier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ImplicationServiceImpl implements ImplicationService {

    private final ImplicationRepository repository;
    private final DossierRepository dossierRepository;
    private final PersonneRepository personneRepository;
    private final RoleImplicationRepository roleImplicationRepository;
    private final EntiteOrganisationRepository entiteOrganisationRepository;
    private final StatutJudiciaireRepository statutJudiciaireRepository;
    private final ImplicationMapper mapper;
    private final AuditService auditService;
    private final JdbcTemplate jdbc;

    @Override
    @Transactional(readOnly = true)
    public List<ImplicationResponse> listerParDossier(UUID dossierId) {
        return repository.findByDossierId(dossierId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void supprimer(UUID dossierId, UUID implicationId) {
        Implication implication = repository.findById(implicationId)
                .filter(i -> i.getDossier().getId().equals(dossierId))
                .orElseThrow(() -> new ResourceNotFoundException("Implication", implicationId));

        RegleDossier.verifierOuvert(implication.getDossier());
        Integer faitsValides = jdbc.queryForObject("""
                select count(*) from dossiers.implication_fait imf
                join dossiers.fait_reproche fr on fr.id = imf.fait_reproche_id
                where imf.implication_id = ? and fr.statut_validation = 'VALIDEE'""", Integer.class, implicationId);
        Integer peines = jdbc.queryForObject("""
                select count(*) from dossiers.peine pe
                join dossiers.implication_fait imf on imf.id = pe.implication_fait_id
                where imf.implication_id = ?""", Integer.class, implicationId);
        if ((faitsValides != null && faitsValides > 0) || (peines != null && peines > 0)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette personne est liee a des faits valides ou a des peines : elle ne peut plus etre retiree du dossier.");
        }

        Map<String, Object> avant = Map.of(
                "dossierId", dossierId.toString(),
                "personneId", implication.getPersonne().getId().toString(),
                "role", String.valueOf(implication.getRoleImplication().getLibelle()));

        // liens de documents et liaisons aux faits (non valides) de cette implication, puis l'implication elle-meme
        jdbc.update("delete from documents.document_implication where implication_id = ?", implicationId);
        jdbc.update("delete from dossiers.implication_fait where implication_id = ?", implicationId);
        repository.delete(implication);
        repository.flush();

        auditService.enregistrer("SUPPRESSION", "Implication", implicationId, avant, null);
    }

    @Override
    public ImplicationResponse creer(UUID dossierId, ImplicationRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", dossierId));

        RegleDossier.verifierOuvert(dossier);

        Personne personne = personneRepository.findById(request.getPersonneId())
                .orElseThrow(() -> new ResourceNotFoundException("Personne", request.getPersonneId()));

        Implication entite = mapper.toEntity(request);
        entite.setDossier(dossier);
        entite.setPersonne(personne);

        entite.setRoleImplication(roleImplicationRepository.findById(request.getRoleImplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Role d'implication", request.getRoleImplicationId())));

        if (request.getEntiteOrganisationId() != null) {
            entite.setEntiteOrganisation(entiteOrganisationRepository.findById(request.getEntiteOrganisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Entite organisation", request.getEntiteOrganisationId())));
        }

        // Nouveau : resolution du statut judiciaire s'il est fourni
        if (request.getStatutJudiciaireId() != null) {
            entite.setStatutJudiciaire(statutJudiciaireRepository.findById(request.getStatutJudiciaireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire", request.getStatutJudiciaireId())));
        }

        if (request.getDateDebut() != null) {
            entite.setDateDebut(request.getDateDebut());
        }

        Implication sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "Implication", sauvegarde.getId(), null,
                Map.of("personneId", personne.getId().toString(),
                       "dossierId", dossier.getId().toString(),
                       "role", sauvegarde.getRoleImplication().getLibelle()));

        return mapper.toResponse(sauvegarde);
    }
}
