// dossier/service/impl/DossierWorkflowServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.AjouterDossierPersonneRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.FaitReprocheRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.OuvrirDossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.AjouterDossierPersonneResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.OuvrirDossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.FaitReprocheMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationFaitRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.DossierWorkflowService;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.PersonneMoraleMapper;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.PersonnePhysiqueMapper;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.RoleImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.SourceSignalementRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.StatutJudiciaireRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypeInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.ZoneGeographiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DossierWorkflowServiceImpl implements DossierWorkflowService {

    private final DossierRepository dossierRepository;
    private final SourceSignalementRepository sourceSignalementRepository;
    private final AuditService auditService;

    private final PersonneRepository personneRepository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final PersonneMoraleRepository personneMoraleRepository;
    private final PersonnePhysiqueMapper personnePhysiqueMapper;
    private final PersonneMoraleMapper personneMoraleMapper;
    private final CurrentUserProvider currentUserProvider;

    private final ImplicationRepository implicationRepository;
    private final RoleImplicationRepository roleImplicationRepository;
    private final EntiteOrganisationRepository entiteOrganisationRepository;
    private final StatutJudiciaireRepository statutJudiciaireRepository;

    private final FaitReprocheRepository faitReprocheRepository;
    private final TypeInfractionRepository typeInfractionRepository;
    private final ZoneGeographiqueRepository zoneGeographiqueRepository;
    private final FaitReprocheMapper faitReprocheMapper;
    private final ImplicationFaitRepository implicationFaitRepository;

    @Override
    public OuvrirDossierResponse ouvrirDossier(OuvrirDossierRequest request) {
        Personne personne = resoudrePersonne(request);

        Dossier dossier = new Dossier();
        dossier.setNumeroDossier(request.getDossier().getNumeroDossier());
        dossier.setIntitule(request.getDossier().getIntitule());
        dossier.setDescriptionContexte(request.getDossier().getDescriptionContexte());
        dossier.setSourceSignalement(sourceSignalementRepository.findById(request.getDossier().getSourceSignalementId())
                .orElseThrow(() -> new ResourceNotFoundException("Source de signalement",
                        request.getDossier().getSourceSignalementId())));
        
        Dossier dossierSauvegarde = dossierRepository.save(dossier);
        
        // PATCH 2 : Auto-génération du numéro si absent ou vide
        if (dossierSauvegarde.getNumeroDossier() == null || dossierSauvegarde.getNumeroDossier().isBlank()) {
            dossierSauvegarde.setNumeroDossier(genererNumeroDossier(dossierSauvegarde.getId()));
        }

        Implication implication = new Implication();
        implication.setPersonne(personne);
        implication.setDossier(dossierSauvegarde);
        implication.setRoleImplication(roleImplicationRepository.findById(request.getRoleImplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Role d'implication", request.getRoleImplicationId())));
        implication.setFonctionOccupee(request.getFonctionOccupee());
        if (request.getEntiteOrganisationId() != null) {
            implication.setEntiteOrganisation(entiteOrganisationRepository.findById(request.getEntiteOrganisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Entite organisation", request.getEntiteOrganisationId())));
        }
        Implication implicationSauvegardee = implicationRepository.save(implication);

        FaitReprocheRequest faitRequest = request.getPremierFait();
        FaitReproche fait = faitReprocheMapper.toEntity(faitRequest);
        fait.setDossier(dossierSauvegarde);
        fait.setTypeInfraction(typeInfractionRepository.findById(faitRequest.getTypeInfractionId())
                .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction", faitRequest.getTypeInfractionId())));
        if (faitRequest.getZoneGeographiqueId() != null) {
            fait.setZoneGeographique(zoneGeographiqueRepository.findById(faitRequest.getZoneGeographiqueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone geographique", faitRequest.getZoneGeographiqueId())));
        }
        fait.setStatutValidation(StatutValidation.EN_ATTENTE);
        FaitReproche faitSauvegarde = faitReprocheRepository.save(fait);
        
        // Liaison ImplicationFait
        creerLiaisonImplicationFait(implicationSauvegardee, faitSauvegarde);

        auditService.enregistrer("OUVERTURE_DOSSIER", "Dossier", dossierSauvegarde.getId(), null,
                Map.of("personneId", personne.getId().toString(),
                       "implicationId", implicationSauvegardee.getId().toString(),
                       "premierFaitId", faitSauvegarde.getId().toString()));

        return OuvrirDossierResponse.builder()
                .personneId(personne.getId())
                .dossierId(dossierSauvegarde.getId())
                .implicationId(implicationSauvegardee.getId())
                .premierFaitId(faitSauvegarde.getId())
                .build();
    }

    @Override
    public AjouterDossierPersonneResponse ajouterDossierAPersonne(UUID personneId, AjouterDossierPersonneRequest request) {
        // 1. Verifier que la personne existe
        Personne personne = personneRepository.findById(personneId)
                .orElseThrow(() -> new ResourceNotFoundException("Personne", personneId));

        // 2. Creer le dossier
        Dossier dossier = new Dossier();
        dossier.setNumeroDossier(request.getDossier().getNumeroDossier());
        dossier.setIntitule(request.getDossier().getIntitule());
        dossier.setDescriptionContexte(request.getDossier().getDescriptionContexte());
        dossier.setSourceSignalement(sourceSignalementRepository.findById(request.getDossier().getSourceSignalementId())
                .orElseThrow(() -> new ResourceNotFoundException("Source de signalement",
                        request.getDossier().getSourceSignalementId())));
        
        Dossier dossierSauvegarde = dossierRepository.save(dossier);

        // PATCH 2 : Auto-génération du numéro si absent ou vide
        if (dossierSauvegarde.getNumeroDossier() == null || dossierSauvegarde.getNumeroDossier().isBlank()) {
            dossierSauvegarde.setNumeroDossier(genererNumeroDossier(dossierSauvegarde.getId()));
        }

        // 3. Creer l'implication
        Implication implication = new Implication();
        implication.setPersonne(personne);
        implication.setDossier(dossierSauvegarde);
        implication.setRoleImplication(roleImplicationRepository.findById(request.getRoleImplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Role d'implication", request.getRoleImplicationId())));
        implication.setFonctionOccupee(request.getFonctionOccupee());
        implication.setAutoriteCompetente(request.getAutoriteCompetente());
        implication.setReferenceAffaire(request.getReferenceAffaire());

        if (request.getEntiteOrganisationId() != null) {
            implication.setEntiteOrganisation(entiteOrganisationRepository.findById(request.getEntiteOrganisationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Entite organisation", request.getEntiteOrganisationId())));
        }

        if (request.getStatutJudiciaireId() != null) {
            implication.setStatutJudiciaire(statutJudiciaireRepository.findById(request.getStatutJudiciaireId())
                    .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire", request.getStatutJudiciaireId())));
        }

        Implication implicationSauvegardee = implicationRepository.save(implication);

        // 4. Creer tous les faits reproches
        List<UUID> faitsIds = new ArrayList<>();
        for (FaitReprocheRequest faitRequest : request.getFaits()) {
            FaitReproche fait = faitReprocheMapper.toEntity(faitRequest);
            fait.setDossier(dossierSauvegarde);
            fait.setTypeInfraction(typeInfractionRepository.findById(faitRequest.getTypeInfractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction", faitRequest.getTypeInfractionId())));
            if (faitRequest.getZoneGeographiqueId() != null) {
                fait.setZoneGeographique(zoneGeographiqueRepository.findById(faitRequest.getZoneGeographiqueId())
                        .orElseThrow(() -> new ResourceNotFoundException("Zone geographique", faitRequest.getZoneGeographiqueId())));
            }
            fait.setStatutValidation(StatutValidation.EN_ATTENTE);
            FaitReproche faitSauvegarde = faitReprocheRepository.save(fait);
            
            // Liaison ImplicationFait pour chaque fait
            creerLiaisonImplicationFait(implicationSauvegardee, faitSauvegarde);
            
            faitsIds.add(faitSauvegarde.getId());
        }

        // 5. Audit unique
        auditService.enregistrer("AJOUT_DOSSIER_PERSONNE", "Dossier", dossierSauvegarde.getId(), null,
                Map.of("personneId", personne.getId().toString(),
                       "implicationId", implicationSauvegardee.getId().toString(),
                       "nombreFaits", String.valueOf(faitsIds.size())));

        return AjouterDossierPersonneResponse.builder()
                .personneId(personne.getId())
                .dossierId(dossierSauvegarde.getId())
                .implicationId(implicationSauvegardee.getId())
                .faitsIds(faitsIds)
                .build();
    }

    private Personne resoudrePersonne(OuvrirDossierRequest request) {
        int nbOptionsFournies = 0;
        if (request.getPersonneExistanteId() != null) nbOptionsFournies++;
        if (request.getNouvellePersonnePhysique() != null) nbOptionsFournies++;
        if (request.getNouvellePersonneMorale() != null) nbOptionsFournies++;

        if (nbOptionsFournies != 1) {
            throw new IllegalArgumentException(
                    "Fournir exactement une option : personneExistanteId, nouvellePersonnePhysique ou nouvellePersonneMorale");
        }

        if (request.getPersonneExistanteId() != null) {
            return personneRepository.findById(request.getPersonneExistanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Personne", request.getPersonneExistanteId()));
        }

        if (request.getNouvellePersonnePhysique() != null) {
            PersonnePhysique entite = personnePhysiqueMapper.toEntity(request.getNouvellePersonnePhysique());
            entite.setCreePar(currentUserProvider.utilisateurCourant());
            PersonnePhysique sauvegarde = personnePhysiqueRepository.save(entite);
            auditService.enregistrer("CREATION", "PersonnePhysique", sauvegarde.getId(), null,
                    Map.of("nomNaissance", sauvegarde.getNomNaissance(), "prenoms", sauvegarde.getPrenoms()));
            return sauvegarde;
        }

        PersonneMorale entite = personneMoraleMapper.toEntity(request.getNouvellePersonneMorale());
        entite.setCreePar(currentUserProvider.utilisateurCourant());
        if (request.getNouvellePersonneMorale().getRepresentantLegalId() != null) {
            entite.setRepresentantLegal(personnePhysiqueRepository
                    .findById(request.getNouvellePersonneMorale().getRepresentantLegalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Personne physique (representant legal)",
                            request.getNouvellePersonneMorale().getRepresentantLegalId())));
        }
        PersonneMorale sauvegarde = personneMoraleRepository.save(entite);
        auditService.enregistrer("CREATION", "PersonneMorale", sauvegarde.getId(), null,
                Map.of("denominationSociale", sauvegarde.getDenominationSociale()));
        return sauvegarde;
    }

    // ====================================================================================
    // METHODES UTILITAIRES
    // ====================================================================================

    /**
     * Génère un numéro de dossier au format DOSS-YYYY-XXXXXXXX
     * basés sur l'année courante et les 8 premiers caractères de l'UUID du dossier.
     */
    private String genererNumeroDossier(UUID dossierId) {
        return String.format("DOSS-%d-%s",
                LocalDate.now().getYear(),
                dossierId.toString().substring(0, 8).toUpperCase());
    }

    /**
     * Choisit le statut judiciaire initial à porter par un ImplicationFait :
     * 1) celui de l'Implication si renseigné,
     * 2) sinon "En instruction" depuis le referentiel,
     * 3) sinon n'importe quel statut actif (sécurité pour ne pas planter).
     */
    private StatutJudiciaire statutJudiciaireInitial(Implication implication) {
        if (implication.getStatutJudiciaire() != null) {
            return implication.getStatutJudiciaire();
        }
        return statutJudiciaireRepository.findByLibelleIgnoreCase("En instruction")
                .or(() -> statutJudiciaireRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun statut judiciaire dans referentiels.statut_judiciaire — peupler la table"));
    }

    private ImplicationFait creerLiaisonImplicationFait(Implication implication, FaitReproche fait) {
        ImplicationFait lien = new ImplicationFait();
        lien.setImplication(implication);
        lien.setFaitReproche(fait);
        lien.setStatutJudiciaire(statutJudiciaireInitial(implication));
        lien.setDateStatut(LocalDate.now());
        return implicationFaitRepository.save(lien);
    }
}
