package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.config.security.JwtRoleUtils;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheDetailResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.FicheMiseEnCauseMapper;
import bf.gov.ascelc.logintegrite_backend.service.NotificationService;
import bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(ApiURLs.FICHES)
public class FicheMiseEnCauseController {

    private final FicheMiseEnCauseService ficheService;
    private final FicheMiseEnCauseMapper ficheMapper;
    private final PersonnePhysiqueService ppService;
    private final PersonneMoraleService pmService;
    private final AuditService auditService;
    private final HttpServletRequest request;
    private final NotificationService notificationService;


    public FicheMiseEnCauseController(

            @Qualifier("ficheMiseEnCauseServiceImpl") FicheMiseEnCauseService ficheService,
            FicheMiseEnCauseMapper ficheMapper,
            PersonnePhysiqueService ppService,
            PersonneMoraleService pmService,
            AuditService auditService,
            NotificationService notificationService,
            HttpServletRequest request) {
        this.ficheService = ficheService;
        this.ficheMapper = ficheMapper;
        this.ppService = ppService;
        this.pmService = pmService;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.request = request;

    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR', 'ROLE_public')")
    public ResponseEntity<?> lister(@AuthenticationPrincipal Jwt jwt) {
        boolean isPublicOnly = JwtRoleUtils.estRolePublicUniquement(jwt);
        if (isPublicOnly) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        boolean isAdmin = JwtRoleUtils.estAdministrateur(jwt);
        boolean isValidateurOnly = JwtRoleUtils.estValidateurUniquement(jwt);

        List<FicheMiseEnCauseResponse> responses = new ArrayList<>();

        if (isAdmin) {
            // Supervision complète : toutes les fiches, tous statuts, tous créateurs
            responses.addAll(ppService.listerTout().stream().map(ficheMapper::toResponse).collect(Collectors.toList()));
            responses.addAll(pmService.listerTout().stream().map(ficheMapper::toResponse).collect(Collectors.toList()));
        } else if (isValidateurOnly) {
            // File d'attente de validation uniquement, tous créateurs confondus
            responses.addAll(ficheService.rechercherFileAttenteValidation(null, null, Pageable.unpaged()).getContent());
        } else {
            // Agent : uniquement ses propres fiches, tous statuts confondus
            String userId = jwt.getSubject();
            responses.addAll(ppService.rechercherMesFiches(userId, null, Pageable.unpaged()).getContent());
            responses.addAll(pmService.rechercherMesFiches(userId, null, Pageable.unpaged()).getContent());
        }

        return ResponseEntity.ok(responses);
    }


    // AJOUT (registre officiel) : recherche unifiée PP+PM, fiches ACTIVE
    // uniquement, pagination fiable côté SQL. Visible par tous les rôles y
    // compris public (c'est le registre officiel, il doit être consultable
    // par tous — décision : identifiantUnique reste visible au public, ce
    // DTO n'expose pas de données sensibles comme la date de naissance).
    @GetMapping(ApiURLs.FICHES_RECHERCHE)
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> registreOfficiel(
            @RequestParam(required = false) String recherche,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) UUID entiteId,
            @RequestParam(required = false) String typeFiche,
            Pageable pageable) {
        return ResponseEntity.ok(ficheService.rechercherRegistreOfficiel(recherche, regionId, entiteId, typeFiche, pageable));
    }

    // Les 5 dernières fiches BROUILLON/EN_ATTENTE_VALIDATION créées par
    // l'utilisateur connecté (PP+PM confondus), pour le dashboard
    @GetMapping(ApiURLs.FICHES_MES_ACTIONS_RECENTES)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<List<FicheMiseEnCauseResponse>> mesActionsRecentes(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";

        List<FicheMiseEnCauseResponse> combinees = new ArrayList<>();
        combinees.addAll(ppService.listerRecentesBrouillonsOuSoumises(userId, 5).stream()
                .map(ficheMapper::toResponse).collect(Collectors.toList()));
        combinees.addAll(pmService.listerRecentesBrouillonsOuSoumises(userId, 5).stream()
                .map(ficheMapper::toResponse).collect(Collectors.toList()));

        List<FicheMiseEnCauseResponse> cinqPlusRecentes = combinees.stream()
                .sorted(Comparator.comparing(FicheMiseEnCauseResponse::getDateModification,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cinqPlusRecentes);
    }

    @GetMapping(ApiURLs.FICHES_ID)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT', 'ROLE_VALIDATEUR')")
    public ResponseEntity<FicheDetailResponse> consulter(@PathVariable UUID id) {
        FicheMiseEnCause fiche = ficheService.consulterAvecDetails(id);
        return ResponseEntity.ok(ficheMapper.toDetailResponse(fiche));
    }

    @PutMapping(ApiURLs.FICHES_SOUMETTRE)
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> soumettre(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        String username = jwt != null ? jwt.getClaimAsString("preferred_username") : "SYSTEM";
        FicheMiseEnCauseResponse response = ficheMapper.toResponse(ficheService.soumettre(id, agentId));
        auditService.log(jwt, "SOUMISSION_FICHE", response.getTypeFiche(), id.toString(), null, request.getRemoteAddr());
        notificationService.notifierRole("VALIDATEUR", "SOUMISSION_FICHE",
                "La fiche " + response.getCibleNom() + " a été soumise par " + username + " et attend votre validation.",
                id.toString(), response.getTypeFiche());
        return ResponseEntity.ok(response);

    }

    @GetMapping(ApiURLs.FICHES_A_VALIDER)
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR', 'ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Page<FicheMiseEnCauseResponse>> fileAttenteValidation(
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) UUID entiteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ficheService.rechercherFileAttenteValidation(regionId, entiteId, pageable));
    }

    // AJOUT : "actions rapides" du validateur — les 5 dernières fiches
    // (PP+PM) qu'IL a lui-même validées ou rejetées, triées par date de
    // décision décroissante.
    @GetMapping(ApiURLs.FICHES_MES_ACTIONS_VALIDATEUR)
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<List<FicheMiseEnCauseResponse>> mesActionsValidateur(@AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";

        List<FicheMiseEnCauseResponse> combinees = new ArrayList<>();
        combinees.addAll(ppService.listerRecentesValideesOuRejeteesParValidateur(validateurId, 5).stream()
                .map(ficheMapper::toResponse).collect(Collectors.toList()));
        combinees.addAll(pmService.listerRecentesValideesOuRejeteesParValidateur(validateurId, 5).stream()
                .map(ficheMapper::toResponse).collect(Collectors.toList()));

        List<FicheMiseEnCauseResponse> cinqPlusRecentes = combinees.stream()
                .sorted(Comparator.comparing(FicheMiseEnCauseResponse::getDateModification,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cinqPlusRecentes);
    }

    // AJOUT : total exact des décisions personnelles du validateur, pour les KPI
// "Validées par moi" / "Rejetées par moi" du tableau de bord.
    @GetMapping(ApiURLs.FICHES_MES_DECISIONS_COMPTEUR)
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<Long> compterMesDecisions(
            @RequestParam String statut, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        long total = ppService.compterDecisionsParValidateur(validateurId, statut)
                + pmService.compterDecisionsParValidateur(validateurId, statut);
        return ResponseEntity.ok(total);
    }

    // AJOUT : liste complète des décisions personnelles du validateur (PP+PM),
    // filtrée par statut — alimente "Mes Rejets" / "Validées par moi" côté
    // frontend (FicheService.getMesDecisions()). Miroir de
    // mesActionsValidateur() ci-dessus, mais sans limite à 5 ni tri : ici
    // c'est une liste consultable en entier, pas un aperçu "actions rapides".
    @GetMapping(ApiURLs.FICHES_MES_DECISIONS)
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<List<FicheMiseEnCauseResponse>> mesDecisions(
            @RequestParam String statut, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";

        List<FicheMiseEnCauseResponse> combinees = new ArrayList<>();
        combinees.addAll(ppService.listerDecisionsParValidateur(validateurId, statut).stream()
                .map(ficheMapper::toResponse).collect(Collectors.toList()));
        combinees.addAll(pmService.listerDecisionsParValidateur(validateurId, statut).stream()
                .map(ficheMapper::toResponse).collect(Collectors.toList()));

        return ResponseEntity.ok(combinees);
    }

    // valider() mis à jour
    @PutMapping(ApiURLs.FICHES_VALIDER)
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";

        FicheMiseEnCause ficheExistante = ficheService.consulter(id);
        FicheMiseEnCause ficheValidee;
        if (ficheExistante instanceof PersonnePhysique) {
            ficheValidee = ppService.valider(id, validateurId);
        } else if (ficheExistante instanceof PersonneMorale) {
            ficheValidee = pmService.valider(id, validateurId);
        } else {
            throw new IllegalStateException(
                    "Type de fiche non pris en charge pour la validation : " + ficheExistante.getClass().getSimpleName());
        }

        FicheMiseEnCauseResponse response = ficheMapper.toResponse(ficheValidee);
        auditService.log(jwt, "VALIDATION_FICHE", response.getTypeFiche(), id.toString(), null, request.getRemoteAddr());
        notificationService.notifierUtilisateur(response.getCreatedById(), "VALIDATION_FICHE",
                "Votre fiche " + response.getCibleNom() + " a été validée.",
                id.toString(), response.getTypeFiche());
        return ResponseEntity.ok(response);
    }

    @PutMapping(ApiURLs.FICHES_REJETER)
    @PreAuthorize("hasAnyAuthority('ROLE_VALIDATEUR')")
    public ResponseEntity<FicheMiseEnCauseResponse> rejeter(
            @PathVariable UUID id, @RequestParam String motif, @AuthenticationPrincipal Jwt jwt) {
        String validateurId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCauseResponse response = ficheMapper.toResponse(ficheService.rejeter(id, motif, validateurId));
        auditService.log(jwt, "REJET_FICHE", response.getTypeFiche(), id.toString(), motif, request.getRemoteAddr());
        notificationService.notifierUtilisateur(response.getCreatedById(), "REJET_FICHE",
                "Votre fiche " + response.getCibleNom() + " a été rejetée. Motif : " + motif,
                id.toString(), response.getTypeFiche());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(ApiURLs.FICHES_ARCHIVER)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR')")
    public ResponseEntity<Void> archiver(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        ficheService.archiver(id);
        auditService.log(jwt, "ARCHIVAGE_FICHE", "FicheMiseEnCause", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(ApiURLs.FICHES_STATUT_JUDICIAIRE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<FicheMiseEnCauseResponse> modifierStatutJudiciaire(
            @PathVariable UUID id, @Valid @RequestBody StatutJudiciaireRequest statutRequest, @AuthenticationPrincipal Jwt jwt) {
        String agentId = jwt != null ? jwt.getSubject() : "SYSTEM";
        FicheMiseEnCauseResponse response = ficheMapper.toResponse(
                ficheService.modifierStatutJudiciaire(id, statutRequest, agentId));
        auditService.log(jwt, "MODIFICATION_STATUT_JUDICIAIRE", response.getTypeFiche(), id.toString(),
                "Nouveau statut : " + statutRequest.getStatutJudiciaire(), request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(ApiURLs.FICHES_ID)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRATEUR', 'ROLE_AGENT')")
    public ResponseEntity<Void> supprimer(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        boolean isAdmin = JwtRoleUtils.estAdministrateur(jwt);
        ficheService.supprimerBrouillonOuSoumise(id, userId, isAdmin);
        auditService.log(jwt, "SUPPRESSION_FICHE", "FicheMiseEnCause", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
