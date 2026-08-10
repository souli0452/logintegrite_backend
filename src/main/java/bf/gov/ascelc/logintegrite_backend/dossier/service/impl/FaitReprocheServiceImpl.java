// dossier/service/impl/FaitReprocheServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.FaitReprocheRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.RejetFaitRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierAValiderResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitRejeteResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitReprocheResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait; // AJOUT
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.FaitReprocheMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationFaitRepository; // AJOUT
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.FaitReprocheService;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire; // AJOUT
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.StatutJudiciaireRepository; // AJOUT
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypeInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.ZoneGeographiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate; // AJOUT
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FaitReprocheServiceImpl implements FaitReprocheService {

    private final FaitReprocheRepository repository;
    private final DossierRepository dossierRepository;
    private final TypeInfractionRepository typeInfractionRepository;
    private final ZoneGeographiqueRepository zoneGeographiqueRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FaitReprocheMapper mapper;
    private final AuditService auditService;
    private final ImplicationRepository implicationRepository;
    
    // AJOUT DES DEPENDANCES
    private final ImplicationFaitRepository implicationFaitRepository;
    private final StatutJudiciaireRepository statutJudiciaireRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FaitReprocheResponse> listerParDossier(UUID dossierId) {
        return repository.findByDossierId(dossierId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public FaitReprocheResponse creer(UUID dossierId, FaitReprocheRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", dossierId));

        FaitReproche entite = mapper.toEntity(request);
        entite.setDossier(dossier);
        entite.setTypeInfraction(typeInfractionRepository.findById(request.getTypeInfractionId())
                .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction", request.getTypeInfractionId())));
        if (request.getZoneGeographiqueId() != null) {
            entite.setZoneGeographique(zoneGeographiqueRepository.findById(request.getZoneGeographiqueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone geographique", request.getZoneGeographiqueId())));
        }
        entite.setStatutValidation(StatutValidation.EN_ATTENTE);
        FaitReproche faitSauvegarde = repository.save(entite);

        // NOUVEAU : lier le fait à toutes les implications existantes du dossier
        List<Implication> implications = implicationRepository.findByDossierId(dossierId);
        for (Implication implication : implications) {
            ImplicationFait lien = new ImplicationFait();
            lien.setImplication(implication);
            lien.setFaitReproche(faitSauvegarde);
            lien.setStatutJudiciaire(statutJudiciaireInitial(implication));
            lien.setDateStatut(LocalDate.now());
            implicationFaitRepository.save(lien);
        }

        auditService.enregistrer("CREATION", "FaitReproche", faitSauvegarde.getId(), null,
                Map.of("description", faitSauvegarde.getDescription(),
                       "montantPrejudice", faitSauvegarde.getMontantPrejudice(),
                       "statutValidation", faitSauvegarde.getStatutValidation().name(),
                       "nombreLiaisonsCreees", String.valueOf(implications.size())));

        return mapper.toResponse(faitSauvegarde);
    }

    @Override
    public FaitReprocheResponse valider(UUID faitId) {
        FaitReproche entite = trouverOuLever(faitId);
        Map<String, Object> avant = Map.of("statutValidation", entite.getStatutValidation().name());

        entite.setStatutValidation(StatutValidation.VALIDEE);
        entite.setValidePar(currentUserProvider.utilisateurCourant());
        entite.setDateValidation(Instant.now());
        entite.setMotifRejet(null);
        FaitReproche sauvegarde = repository.save(entite);

        auditService.enregistrer("VALIDATION", "FaitReproche", sauvegarde.getId(), avant,
                Map.of("statutValidation", "VALIDEE"));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public FaitReprocheResponse rejeter(UUID faitId, RejetFaitRequest request) {
        FaitReproche entite = trouverOuLever(faitId);
        Map<String, Object> avant = Map.of("statutValidation", entite.getStatutValidation().name());

        entite.setStatutValidation(StatutValidation.REJETEE);
        entite.setMotifRejet(request.getMotifRejet());
        entite.setValidePar(currentUserProvider.utilisateurCourant());
        entite.setDateValidation(Instant.now());
        FaitReproche sauvegarde = repository.save(entite);

        auditService.enregistrer("REJET", "FaitReproche", sauvegarde.getId(), avant,
                Map.of("statutValidation", "REJETEE", "motifRejet", request.getMotifRejet()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FaitReprocheResponse> listerParStatut(StatutValidation statut, Pageable pageable) {
        return repository.findByStatutValidation(statut, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DossierAValiderResponse> listerDossiersAvecFaitsEnAttente() {
        // 1. Recuperer tous les faits EN_ATTENTE (peu importe la volumetrie
        //    car on est en interne au SI, quelques centaines maxi)
        List<FaitReproche> faitsEnAttente = repository.findByStatutValidation(
            StatutValidation.EN_ATTENTE,
            Pageable.unpaged()
        ).getContent();

        if (faitsEnAttente.isEmpty()) {
            return List.of();
        }

        // 2. Regrouper par dossier (LinkedHashMap pour conserver l'ordre d'insertion)
        Map<UUID, List<FaitReproche>> parDossier = new LinkedHashMap<>();
        for (FaitReproche fait : faitsEnAttente) {
            parDossier.computeIfAbsent(fait.getDossier().getId(), k -> new ArrayList<>()).add(fait);
        }

        // 3. Pour chaque dossier, construire la reponse enrichie avec les infos personne
        List<DossierAValiderResponse> resultat = new ArrayList<>();
        for (Map.Entry<UUID, List<FaitReproche>> entree : parDossier.entrySet()) {
            Dossier dossier = entree.getValue().get(0).getDossier();
            List<FaitReproche> faitsDuDossier = entree.getValue();

            // Trouver la premiere implication du dossier (par date de creation)
            // Elle sert de "personne principale" a afficher dans la carte
            List<Implication> implications = implicationRepository.findByDossierId(dossier.getId());
            Implication implicationPrincipale = implications.stream()
                    .min(Comparator.comparing(Implication::getDateCreation,
                         Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);

            DossierAValiderResponse.DossierAValiderResponseBuilder builder = DossierAValiderResponse.builder()
                .dossierId(dossier.getId())
                .numeroDossier(dossier.getNumeroDossier())
                .intitule(dossier.getIntitule())
                .dateOuverture(dossier.getDateOuverture())
                .sourceSignalementLibelle(
                    dossier.getSourceSignalement() != null ? dossier.getSourceSignalement().getLibelle() : null)
                .faitsEnAttente(faitsDuDossier.stream().map(mapper::toResponse).toList())
                .nombreFaitsEnAttente(faitsDuDossier.size());

            if (implicationPrincipale != null) {
                builder
                    .personneId(implicationPrincipale.getPersonne().getId())
                    .personneNomAffichage(implicationPrincipale.getPersonne().getNomAffichage())
                    .personneTypePersonne(implicationPrincipale.getPersonne().getTypePersonne().name())
                    .personneRoleImplication(
                        implicationPrincipale.getRoleImplication() != null
                            ? implicationPrincipale.getRoleImplication().getLibelle()
                            : null);
            }

            resultat.add(builder.build());
        }

        return resultat;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaitRejeteResponse> listerRejetes() {
        List<FaitReproche> rejetes = repository.findByStatutValidation(StatutValidation.REJETEE);

        return rejetes.stream()
                .sorted((a, b) -> {
                    if (a.getDateValidation() == null) return 1;
                    if (b.getDateValidation() == null) return -1;
                    return b.getDateValidation().compareTo(a.getDateValidation());
                })
                .map(this::versFaitRejeteResponse)
                .toList();
    }

    @Override
    public FaitReprocheResponse reprendre(UUID faitId) {
        FaitReproche fait = repository.findById(faitId)
                .orElseThrow(() -> new ResourceNotFoundException("FaitReproche", faitId));

        if (fait.getStatutValidation() != StatutValidation.REJETEE) {
            throw new IllegalStateException("Seuls les faits rejetes peuvent etre repris en edition");
        }

        fait.setStatutValidation(StatutValidation.EN_ATTENTE);
        fait.setMotifRejet(null);
        fait.setDateValidation(null);
        fait.setValidePar(null);
        fait = repository.save(fait);

        auditService.enregistrer("REPRISE_FAIT_REJETE", "FaitReproche", faitId, null,
                Map.of("nouveauStatut", "EN_ATTENTE"));

        return mapper.toResponse(fait);
    }

    // Helper : construit le DTO enrichi avec contexte dossier + personne
    private FaitRejeteResponse versFaitRejeteResponse(FaitReproche fait) {
        Dossier dossier = fait.getDossier();

        // Recupere la premiere implication du dossier pour l'affichage
        List<Implication> implications = implicationRepository.findByDossierId(dossier.getId());
        Implication premiereImpl = implications.isEmpty() ? null : implications.get(0);

        String personneTypeStr = null;
        String personneNom = null;
        UUID personneId = null;
        if (premiereImpl != null && premiereImpl.getPersonne() != null) {
            personneId = premiereImpl.getPersonne().getId();
            personneNom = premiereImpl.getPersonne().getNomAffichage();
            personneTypeStr = premiereImpl.getPersonne().getTypePersonne() != null
                    ? premiereImpl.getPersonne().getTypePersonne().name() : null;
        }

        String rejetePar = null;
        if (fait.getValidePar() != null) {
            rejetePar = fait.getValidePar().getPrenom() + " " + fait.getValidePar().getNom();
        }

        return FaitRejeteResponse.builder()
                .id(fait.getId())
                .dossierId(dossier.getId())
                .numeroDossier(dossier.getNumeroDossier())
                .intitule(dossier.getIntitule())
                .personneId(personneId)
                .personneNomAffichage(personneNom)
                .personneTypePersonne(personneTypeStr)
                .typeInfractionLibelle(fait.getTypeInfraction() != null
                        ? fait.getTypeInfraction().getLibelle() : null)
                .dateFaits(fait.getDateFaits())
                .description(fait.getDescription())
                .montantPrejudice(fait.getMontantPrejudice())
                .devise(fait.getDevise())
                .motifRejet(fait.getMotifRejet())
                .dateRejet(fait.getDateValidation())
                .rejeteParNomComplet(rejetePar)
                .build();
    }

    private FaitReproche trouverOuLever(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fait reproche", id));
    }

    // ====================================================================================
    // METHODE UTILITAIRE (PATCH 3/3)
    // ====================================================================================

    private StatutJudiciaire statutJudiciaireInitial(Implication implication) {
        if (implication.getStatutJudiciaire() != null) {
            return implication.getStatutJudiciaire();
        }
        return statutJudiciaireRepository.findByLibelleIgnoreCase("En instruction")
                .or(() -> statutJudiciaireRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun statut judiciaire dans referentiels.statut_judiciaire"));
    }
}
