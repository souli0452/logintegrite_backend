// personne/specification/PersonneSpecifications.java (complet, methode composite ajoutee a la fin)
package bf.gov.ascelc.logintegrite_backend.personne.specification;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PieceIdentite;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class PersonneSpecifications {

    private PersonneSpecifications() {
    }

    public static Specification<Personne> nomAffichageContient(String texte) {
        return (root, query, cb) -> {
            if (texte == null || texte.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("nomAffichage")), "%" + texte.toLowerCase() + "%");
        };
    }

    public static Specification<Personne> estDeType(TypePersonne type) {
        return (root, query, cb) -> {
            if (type == null) return cb.conjunction();
            return cb.equal(root.get("typePersonne"), type);
        };
    }

    public static Specification<Personne> aNationalite(String nationalite) {
        return (root, query, cb) -> {
            if (nationalite == null || nationalite.isBlank()) return cb.conjunction();
            Root<PersonnePhysique> physique = cb.treat(root, PersonnePhysique.class);
            return cb.equal(cb.lower(physique.get("nationalite")), nationalite.toLowerCase());
        };
    }

    public static Specification<Personne> aNumeroPieceIdentite(String numero) {
        return (root, query, cb) -> {
            if (numero == null || numero.isBlank()) return cb.conjunction();
            Root<PersonnePhysique> physique = cb.treat(root, PersonnePhysique.class);
            Join<PersonnePhysique, PieceIdentite> pieces = physique.join("piecesIdentite");
            query.distinct(true);
            return cb.equal(pieces.get("numero"), numero);
        };
    }

    public static Specification<Personne> aRccm(String rccm) {
        return (root, query, cb) -> {
            if (rccm == null || rccm.isBlank()) return cb.conjunction();
            Root<PersonneMorale> morale = cb.treat(root, PersonneMorale.class);
            return cb.equal(morale.get("rccm"), rccm);
        };
    }

    public static Specification<Personne> aIfu(String ifu) {
        return (root, query, cb) -> {
            if (ifu == null || ifu.isBlank()) return cb.conjunction();
            Root<PersonneMorale> morale = cb.treat(root, PersonneMorale.class);
            return cb.equal(morale.get("ifu"), ifu);
        };
    }

    public static Specification<Personne> impliqueeDansFaitDeType(UUID typeInfractionId) {
        return (root, query, cb) -> {
            if (typeInfractionId == null) return cb.conjunction();
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<ImplicationFait> ifRoot = sub.from(ImplicationFait.class);
            sub.select(ifRoot.get("implication").get("personne").get("id"))
                    .where(cb.equal(ifRoot.get("faitReproche").get("typeInfraction").get("id"), typeInfractionId));
            return root.get("id").in(sub);
        };
    }

    public static Specification<Personne> impliqueeDansZone(UUID zoneGeographiqueId) {
        return (root, query, cb) -> {
            if (zoneGeographiqueId == null) return cb.conjunction();
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<ImplicationFait> ifRoot = sub.from(ImplicationFait.class);
            sub.select(ifRoot.get("implication").get("personne").get("id"))
                    .where(cb.equal(ifRoot.get("faitReproche").get("zoneGeographique").get("id"), zoneGeographiqueId));
            return root.get("id").in(sub);
        };
    }

    public static Specification<Personne> aStatutJudiciaire(UUID statutJudiciaireId) {
        return (root, query, cb) -> {
            if (statutJudiciaireId == null) return cb.conjunction();
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<ImplicationFait> ifRoot = sub.from(ImplicationFait.class);
            sub.select(ifRoot.get("implication").get("personne").get("id"))
                    .where(cb.equal(ifRoot.get("statutJudiciaire").get("id"), statutJudiciaireId));
            return root.get("id").in(sub);
        };
    }

    public static Specification<Personne> dansPeriode(LocalDate debut, LocalDate fin) {
        return (root, query, cb) -> {
            if (debut == null && fin == null) return cb.conjunction();
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<ImplicationFait> ifRoot = sub.from(ImplicationFait.class);
            var dateFaits = ifRoot.get("faitReproche").get("dateFaits").as(LocalDate.class);
            Predicate condition = (debut != null && fin != null) ? cb.between(dateFaits, debut, fin)
                    : (debut != null) ? cb.greaterThanOrEqualTo(dateFaits, debut)
                    : cb.lessThanOrEqualTo(dateFaits, fin);
            sub.select(ifRoot.get("implication").get("personne").get("id")).where(condition);
            return root.get("id").in(sub);
        };
    }

    public static Specification<Personne> rattacheeAEntite(UUID entiteOrganisationId) {
        return (root, query, cb) -> {
            if (entiteOrganisationId == null) return cb.conjunction();
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<Implication> impRoot = sub.from(Implication.class);
            sub.select(impRoot.get("personne").get("id"))
                    .where(cb.equal(impRoot.get("entiteOrganisation").get("id"), entiteOrganisationId));
            return root.get("id").in(sub);
        };
    }

    public static Specification<Personne> aFonction(String fonction) {
        return (root, query, cb) -> {
            if (fonction == null || fonction.isBlank()) return cb.conjunction();
            Subquery<UUID> sub = query.subquery(UUID.class);
            Root<Implication> impRoot = sub.from(Implication.class);
            sub.select(impRoot.get("personne").get("id"))
                    .where(cb.like(cb.lower(impRoot.get("fonctionOccupee")), "%" + fonction.toLowerCase() + "%"));
            return root.get("id").in(sub);
        };
    }

    // Point d'entree unique, utilise par PersonneServiceImpl.rechercher() ET
    // RapportServiceImpl.genererExcelRecherchePersonnes() - evite que les deux
    // divergent silencieusement si un critere est ajoute plus tard.
    public static Specification<Personne> depuisCriteres(PersonneSearchCriteria criteria) {
        return Specification
                .where(nomAffichageContient(criteria.getNomOuDenomination()))
                .and(estDeType(criteria.getTypePersonne()))
                .and(aNationalite(criteria.getNationalite()))
                .and(aNumeroPieceIdentite(criteria.getNumeroPieceIdentite()))
                .and(aRccm(criteria.getRccm()))
                .and(aIfu(criteria.getIfu()))
                .and(impliqueeDansFaitDeType(criteria.getTypeInfractionId()))
                .and(impliqueeDansZone(criteria.getZoneGeographiqueId()))
                .and(aStatutJudiciaire(criteria.getStatutJudiciaireId()))
                .and(rattacheeAEntite(criteria.getEntiteOrganisationId()))
                .and(aFonction(criteria.getFonction()))
                .and(dansPeriode(criteria.getPeriodeDebut(), criteria.getPeriodeFin()));
    }
}
