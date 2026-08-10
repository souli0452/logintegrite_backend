// personne/service/impl/PersonneServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.personne.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.PersonneDocumentResponse;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneResumeResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutAncrage;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonneService;
import bf.gov.ascelc.logintegrite_backend.personne.specification.PersonneSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonneServiceImpl implements PersonneService {

    private final PersonneRepository repository;
    private final ConsultationService consultationService;
    private final JdbcTemplate jdbc;

    @Override
    public Page<PersonneResumeResponse> rechercher(PersonneSearchCriteria criteria, Pageable pageable) {
        // 1. Si pas de filtre statutAncrage : chemin nominal avec pagination SQL native (très rapide)
        if (criteria.getStatutAncrage() == null) {
            Page<Personne> page = repository.findAll(
                PersonneSpecifications.depuisCriteres(criteria), pageable);
            List<UUID> ids = page.getContent().stream().map(Personne::getId).toList();
            Map<UUID, Integer> compteurs = calculerNombreDossiersValidesParPersonne(ids);
            List<PersonneResumeResponse> dtos = page.getContent().stream()
                .map((p) -> versResumeAvecAncrage(p, compteurs.getOrDefault(p.getId(), 0)))
                .toList();
            return new PageImpl<>(dtos, pageable, page.getTotalElements());
        }

        // 2. Sinon : on charge TOUTES les personnes matching, on calcule le statut d'ancrage, on filtre, puis on pagine en Java.
        //    Acceptable à l'échelle actuelle (quelques milliers de personnes max).
        //    TODO: Si la volumétrie dépasse ~10k personnes, remplacer par une subquery SQL sur le count de dossiers validés.
        List<Personne> toutes = repository.findAll(PersonneSpecifications.depuisCriteres(criteria));
        List<UUID> ids = toutes.stream().map(Personne::getId).toList();
        Map<UUID, Integer> compteurs = calculerNombreDossiersValidesParPersonne(ids);

        List<PersonneResumeResponse> filtres = toutes.stream()
            .map((p) -> versResumeAvecAncrage(p, compteurs.getOrDefault(p.getId(), 0)))
            .filter((d) -> d.getStatutAncrage() == criteria.getStatutAncrage())
            .toList();

        // 3. Pagination manuelle sur la liste filtrée
        int total = filtres.size();
        int debut = (int) pageable.getOffset();
        int fin = Math.min(debut + pageable.getPageSize(), total);
        List<PersonneResumeResponse> pageDto = debut >= total ? List.of() : filtres.subList(debut, fin);

        return new PageImpl<>(pageDto, pageable, total);
    }

    @Override
    public PersonneResumeResponse obtenir(UUID id) {
        Personne personne = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personne", id));
        consultationService.enregistrer("Personne", id);

        Map<UUID, Integer> nbDossiersValides = calculerNombreDossiersValidesParPersonne(List.of(id));
        return versResumeAvecAncrage(personne, nbDossiersValides.getOrDefault(id, 0));
    }

    @Override
public List<Map<String, Object>> historiqueStatutsJudiciaires(UUID personneId) {
    if (!repository.existsById(personneId)) {
        throw new ResourceNotFoundException("Personne", personneId);
    }
    
    return jdbc.queryForList(
        "SELECT ja.action, " +
        "       ja.entite_cible AS \"entiteCible\", " +
        "       ja.entite_cible_id AS \"entiteCibleId\", " +
        "       ja.valeur_avant AS \"valeurAvant\", " +
        "       ja.valeur_apres AS \"valeurApres\", " +
        "       ja.date_action AS \"dateAction\", " +
        "       u.nom_complet AS \"utilisateur\" " +
        "FROM audit.journal_audit ja " +
        "LEFT JOIN securite.utilisateur u ON u.id = ja.utilisateur_id " +
        "WHERE ( " +
        "    -- Modifications directes sur l'implication (création, suppression, MAJ observations…) " +
        "    (ja.entite_cible = 'Implication' " +
        "     AND ja.entite_cible_id IN ( " +
        "         SELECT id FROM dossiers.implication WHERE personne_id = ? " +
        "     )) " +
        "  OR " +
        "    -- Modifications sur la liaison implication-fait (changements de statut judiciaire) " +
        "    (ja.entite_cible IN ('ImplicationFait', 'LiaisonFait') " +
        "     AND ja.entite_cible_id IN ( " +
        "         SELECT lf.id FROM dossiers.implication_fait lf " +
        "         JOIN dossiers.implication i ON i.id = lf.implication_id " +
        "         WHERE i.personne_id = ? " +
        "     )) " +
        ") " +
        "ORDER BY ja.date_action DESC",
        personneId, personneId
    );
}

    @Override
    public List<PersonneDocumentResponse> listerDocuments(UUID personneId) {
        if (!repository.existsById(personneId)) {
            throw new ResourceNotFoundException("Personne", personneId);
        }

        return jdbc.queryForList(
            "SELECT DISTINCT " +
            "  d.id AS \"id\", " +
            "  d.dossier_id AS \"dossierId\", " +
            "  dos.numero_dossier AS \"numeroDossier\", " +
            "  dos.intitule AS \"intituleDossier\", " +
            "  d.type_document_id AS \"typeDocumentId\", " +
            "  td.libelle AS \"typeDocumentLibelle\", " +
            "  d.nom_original AS \"nomOriginal\", " +
            "  d.taille_octets AS \"tailleOctets\", " +
            "  d.type_mime AS \"typeMime\", " +
            "  d.hash_integrite AS \"hashIntegrite\", " +
            "  d.date_upload AS \"dateUpload\" " +
            "FROM documents.document d " +
            "JOIN dossiers.dossier dos ON dos.id = d.dossier_id " +
            "JOIN referentiels.type_document td ON td.id = d.type_document_id " +
            "JOIN dossiers.implication imp ON imp.dossier_id = d.dossier_id " +
            "WHERE imp.personne_id = ? " +
            "ORDER BY d.date_upload DESC",
            personneId
        ).stream().map(this::vermPersonneDocumentResponse).toList();
    }

    // Calcule pour un lot de personnes leur nombre de dossiers entièrement validés.
    // Un dossier est "entièrement validé" quand tous ses faits reprochés sont en VALIDEE.
    private Map<UUID, Integer> calculerNombreDossiersValidesParPersonne(List<UUID> personneIds) {
        if (personneIds.isEmpty()) return Map.of();
        
        String placeholders = String.join(",", Collections.nCopies(personneIds.size(), "?"));

        String sql =
            "SELECT sub.personne_id, COUNT(*) AS nb " +
            "FROM ( " +
            "    SELECT imp.personne_id, dos.id AS dossier_id " +
            "    FROM dossiers.dossier dos " +
            "    JOIN dossiers.implication imp ON imp.dossier_id = dos.id " +
            "    WHERE imp.personne_id IN (" + placeholders + ") " +
            "    AND EXISTS (SELECT 1 FROM dossiers.fait_reproche fr WHERE fr.dossier_id = dos.id) " +
            "    AND NOT EXISTS ( " +
            "        SELECT 1 FROM dossiers.fait_reproche fr " +
            "        WHERE fr.dossier_id = dos.id " +
            "        AND fr.statut_validation <> 'VALIDEE' " +
            "    ) " +
            ") sub " +
            "GROUP BY sub.personne_id";

        Map<UUID, Integer> resultat = new HashMap<>();
        jdbc.query(sql, personneIds.toArray(), (rs) -> {
            resultat.put((UUID) rs.getObject("personne_id"), rs.getInt("nb"));
        });
        return resultat;
    }

    // Convertit une ligne SQL brute en DTO typé
    private PersonneDocumentResponse vermPersonneDocumentResponse(Map<String, Object> ligne) {
        return PersonneDocumentResponse.builder()
            .id((UUID) ligne.get("id"))
            .dossierId((UUID) ligne.get("dossierId"))
            .numeroDossier((String) ligne.get("numeroDossier"))
            .intituleDossier((String) ligne.get("intituleDossier"))
            .typeDocumentId((UUID) ligne.get("typeDocumentId"))
            .typeDocumentLibelle((String) ligne.get("typeDocumentLibelle"))
            .nomOriginal((String) ligne.get("nomOriginal"))
            .tailleOctets(((Number) ligne.get("tailleOctets")).longValue())
            .typeMime((String) ligne.get("typeMime"))
            .hashIntegrite((String) ligne.get("hashIntegrite"))
            .dateUpload(((Timestamp) ligne.get("dateUpload")).toInstant())
            .build();
    }

    // Enrichit un PersonneResumeResponse avec son statut d'ancrage calculé
    private PersonneResumeResponse versResumeAvecAncrage(Personne p, int nombreDossiersValides) {
        StatutAncrage statut = nombreDossiersValides > 0
            ? StatutAncrage.REGISTRE_OFFICIEL
            : StatutAncrage.EN_INSTRUCTION;

        return PersonneResumeResponse.builder()
            .id(p.getId())
            .typePersonne(p.getTypePersonne())
            .nomAffichage(p.getNomAffichage())
            .statutAncrage(statut)
            .nombreDossiersValides(nombreDossiersValides)
            .dateCreation(p.getDateCreation())
            .build();
    }
}
