package bf.gov.ascelc.logintegrite_backend.verification.service;

import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.audit.service.JournalSecuriteService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.ProfilUtilisateur;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Criteres;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Dossier;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Fait;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Fiche;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Peine;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Resultat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Verification de l'implication d'UNE personne precise, pour les comptes de consultation.
 *
 * Regles voulues par l'institution :
 *  - on ne cherche pas dans une liste : il faut identifier la personne (numero de piece, RCCM, IFU, numero de personne,
 *    ou nom + prenoms + date de naissance) ; au plus 5 resultats ;
 *  - seules les personnes du REGISTRE OFFICIEL sont visibles (au moins un dossier dont TOUS les faits sont valides) :
 *    une personne seulement "en instruction" n'apparait pas, et son existence n'est pas revelee ;
 *  - la fiche ne contient que les dossiers entierement valides, avec les faits, les statuts judiciaires (dont les
 *    relaxes et acquittements) et les peines ; ni documents, ni pieces d'identite, ni adresse, ni historique interne ;
 *  - chaque recherche et chaque fiche ouverte sont journalisees ; un compte de consultation seule est limite a
 *    20 recherches par 10 minutes.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    static final int MAX_RESULTATS = 5;
    static final int MAX_RECHERCHES = 20;
    static final Duration FENETRE = Duration.ofMinutes(10);
    static final String TYPE_RECHERCHE = "VERIFICATION_RECHERCHE";

    /** Au moins un dossier de la personne dont tous les faits sont valides (definition du registre officiel). */
    static final String DANS_LE_REGISTRE = """
            exists (select 1 from dossiers.implication imp
                    join dossiers.dossier dos on dos.id = imp.dossier_id
                    where imp.personne_id = p.id
                      and exists (select 1 from dossiers.fait_reproche fr where fr.dossier_id = dos.id)
                      and not exists (select 1 from dossiers.fait_reproche fr
                                      where fr.dossier_id = dos.id and fr.statut_validation <> 'VALIDEE'))
            """;

    private final JdbcTemplate jdbc;
    private final ConsultationService consultationService;
    private final JournalSecuriteService journal;

    // ------------------------------------------------------------------ recherche

    @Transactional
    public List<Resultat> rechercher(Criteres c) {
        verifierCriteres(c);
        limiterLeDebit();

        StringBuilder where = new StringBuilder(" where " + DANS_LE_REGISTRE);
        List<Object> args = new ArrayList<>();
        if (rempli(c.numeroPiece())) {
            where.append(" and (exists (select 1 from personnes.piece_identite pi where pi.personne_physique_id = p.id"
                    + " and lower(trim(pi.numero)) = lower(trim(?))) or upper(pp.nip) = upper(trim(?)))");
            args.add(c.numeroPiece());
            args.add(c.numeroPiece());
        }
        if (rempli(c.rccm())) {
            where.append(" and exists (select 1 from personnes.personne_morale m where m.id = p.id"
                    + " and lower(trim(m.rccm)) = lower(trim(?)))");
            args.add(c.rccm());
        }
        if (rempli(c.ifu())) {
            where.append(" and exists (select 1 from personnes.personne_morale m where m.id = p.id"
                    + " and lower(trim(m.ifu)) = lower(trim(?)))");
            args.add(c.ifu());
        }
        if (rempli(c.numeroPersonne())) {
            where.append(" and upper(trim(p.numero_personne)) = upper(trim(?))");
            args.add(c.numeroPersonne());
        }
        if (rempli(c.nom())) {
            where.append(" and (lower(trim(pp.nom_naissance)) = lower(trim(?)) or lower(trim(pp.nom_usage)) = lower(trim(?)))");
            args.add(c.nom());
            args.add(c.nom());
            where.append(" and pp.date_naissance = ?");
            args.add(Date.valueOf(c.dateNaissance()));
            // prenoms complets, ou premier prenom seul
            where.append(" and (lower(trim(pp.prenoms)) = lower(trim(?))"
                    + " or starts_with(lower(trim(pp.prenoms)), lower(trim(?)) || ' '))");
            args.add(c.prenoms());
            args.add(c.prenoms());
        }

        String sql = """
                select p.id, p.numero_personne, p.nom_affichage, p.type_personne::text as type_personne,
                       pp.date_naissance, pp.nationalite,
                       (select count(*) from dossiers.implication imp
                         join dossiers.dossier dos on dos.id = imp.dossier_id
                        where imp.personne_id = p.id
                          and exists (select 1 from dossiers.fait_reproche fr where fr.dossier_id = dos.id)
                          and not exists (select 1 from dossiers.fait_reproche fr
                                          where fr.dossier_id = dos.id and fr.statut_validation <> 'VALIDEE')) as nb
                from personnes.personne p
                left join personnes.personne_physique pp on pp.id = p.id
                """ + where + " order by p.nom_affichage limit " + MAX_RESULTATS;

        List<Resultat> resultats = jdbc.query(sql, (rs, i) -> new Resultat(
                (UUID) rs.getObject("id"), rs.getString("numero_personne"), rs.getString("nom_affichage"),
                rs.getString("type_personne"), localDate(rs.getDate("date_naissance")),
                rs.getString("nationalite"), rs.getInt("nb")), args.toArray());

        journal.enregistrerEvenementServeur(TYPE_RECHERCHE, "/verification",
                decrire(c) + " -> " + resultats.size() + " resultat(s)");
        return resultats;
    }

    static void verifierCriteres(Criteres c) {
        boolean identite = rempli(c.nom()) || rempli(c.prenoms()) || c.dateNaissance() != null;
        boolean identiteComplete = longueur(c.nom()) >= 2 && longueur(c.prenoms()) >= 2 && c.dateNaissance() != null;
        boolean numero = longueur(c.numeroPiece()) >= 4 || longueur(c.rccm()) >= 4
                || longueur(c.ifu()) >= 4 || longueur(c.numeroPersonne()) >= 4;
        if (identite && !identiteComplete) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Pour chercher par identite, indiquez le nom, les prenoms ET la date de naissance.");
        }
        if (!identiteComplete && !numero) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Precisez un numero de piece d'identite, un RCCM, un IFU, ou le nom, les prenoms et la date de naissance.");
        }
    }

    private void limiterLeDebit() {
        if (ProfilUtilisateur.estConsultantSeul()
                && journal.compterDepuis(TYPE_RECHERCHE, Instant.now().minus(FENETRE)) >= MAX_RECHERCHES) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Trop de recherches en peu de temps. Reessayez dans quelques minutes.");
        }
    }

    private static String decrire(Criteres c) {
        List<String> parts = new ArrayList<>();
        if (rempli(c.numeroPiece())) parts.add("piece=" + c.numeroPiece());
        if (rempli(c.rccm())) parts.add("rccm=" + c.rccm());
        if (rempli(c.ifu())) parts.add("ifu=" + c.ifu());
        if (rempli(c.numeroPersonne())) parts.add("numero=" + c.numeroPersonne());
        if (rempli(c.nom())) parts.add("identite=" + c.nom() + " " + c.prenoms() + " ne(e) le " + c.dateNaissance());
        return String.join(" ; ", parts);
    }

    // ------------------------------------------------------------------ fiche

    @Transactional
    public Fiche obtenirFiche(UUID personneId) {
        Map<String, Object> identite = jdbc.query("""
                select p.id, p.numero_personne, p.nom_affichage, p.type_personne::text as type_personne,
                       pp.date_naissance, pp.nationalite, pp.profession,
                       pm.forme_juridique, pm.sigle
                from personnes.personne p
                left join personnes.personne_physique pp on pp.id = p.id
                left join personnes.personne_morale pm on pm.id = p.id
                where p.id = ?""" + " and " + DANS_LE_REGISTRE, rs -> {
            if (!rs.next()) return null;
            Map<String, Object> m = new HashMap<>();
            m.put("numero", rs.getString("numero_personne"));
            m.put("nom", rs.getString("nom_affichage"));
            m.put("type", rs.getString("type_personne"));
            m.put("naissance", localDate(rs.getDate("date_naissance")));
            m.put("nationalite", rs.getString("nationalite"));
            m.put("profession", rs.getString("profession"));
            m.put("forme", rs.getString("forme_juridique"));
            m.put("sigle", rs.getString("sigle"));
            return m;
        }, personneId);
        // Meme reponse que "inconnue" : on ne revele pas qu'une personne existe mais n'est pas encore validee.
        if (identite == null) throw new ResourceNotFoundException("Personne", personneId);

        consultationService.enregistrer("Personne", personneId);

        // Peines, par liaison implication-fait
        Map<UUID, List<Peine>> peines = new HashMap<>();
        jdbc.query("""
                select pe.implication_fait_id, pe.type_peine::text as type_peine, pe.nature_sanction::text as nature,
                       pe.duree, pe.montant_amende, pe.date_decision, pe.description
                from dossiers.peine pe
                join dossiers.implication_fait imf on imf.id = pe.implication_fait_id
                join dossiers.implication imp on imp.id = imf.implication_id
                where imp.personne_id = ?
                order by pe.date_decision
                """, rs -> {
            peines.computeIfAbsent((UUID) rs.getObject("implication_fait_id"), k -> new ArrayList<>()).add(new Peine(
                    rs.getString("type_peine"), rs.getString("nature"), rs.getString("duree"),
                    rs.getBigDecimal("montant_amende"), localDate(rs.getDate("date_decision")),
                    rs.getString("description")));
        }, personneId);

        // Faits (uniquement les faits valides), par implication
        Map<UUID, List<Fait>> faitsParImplication = new HashMap<>();
        jdbc.query("""
                select imf.id as imf_id, imf.implication_id, fr.id as fait_id, ti.libelle as type_infraction,
                       ci.libelle as categorie, fr.date_faits, fr.lieu_precis, fr.description, fr.montant_prejudice,
                       fr.devise, fr.montant_confirme_justice, sj.libelle as statut, imf.date_statut
                from dossiers.implication_fait imf
                join dossiers.implication imp on imp.id = imf.implication_id
                join dossiers.fait_reproche fr on fr.id = imf.fait_reproche_id
                join referentiels.type_infraction ti on ti.id = fr.type_infraction_id
                left join referentiels.categorie_infraction ci on ci.id = ti.categorie_infraction_id
                join referentiels.statut_judiciaire sj on sj.id = imf.statut_judiciaire_id
                where imp.personne_id = ? and fr.statut_validation = 'VALIDEE'
                order by fr.date_faits desc
                """, rs -> {
            UUID imfId = (UUID) rs.getObject("imf_id");
            String statut = rs.getString("statut");
            faitsParImplication.computeIfAbsent((UUID) rs.getObject("implication_id"), k -> new ArrayList<>()).add(new Fait(
                    (UUID) rs.getObject("fait_id"), rs.getString("type_infraction"), rs.getString("categorie"),
                    localDate(rs.getDate("date_faits")), rs.getString("lieu_precis"), rs.getString("description"),
                    rs.getBigDecimal("montant_prejudice"), rs.getString("devise"), rs.getBigDecimal("montant_confirme_justice"),
                    statut, localDate(rs.getDate("date_statut")), estIssueFavorable(statut),
                    peines.getOrDefault(imfId, List.of())));
        }, personneId);

        // Dossiers entierement valides ou la personne est impliquee
        List<Dossier> dossiers = jdbc.query("""
                select dos.id, dos.numero_dossier, dos.intitule, dos.date_ouverture, dos.statut_dossier::text as statut,
                       imp.id as implication_id, ri.libelle as role, imp.fonction_occupee, imp.entite_libelle_a_l_epoque,
                       imp.date_debut, imp.date_fin
                from dossiers.implication imp
                join dossiers.dossier dos on dos.id = imp.dossier_id
                join referentiels.role_implication ri on ri.id = imp.role_implication_id
                where imp.personne_id = ?
                  and exists (select 1 from dossiers.fait_reproche fr where fr.dossier_id = dos.id)
                  and not exists (select 1 from dossiers.fait_reproche fr
                                  where fr.dossier_id = dos.id and fr.statut_validation <> 'VALIDEE')
                order by dos.date_ouverture desc
                """, (rs, i) -> new Dossier(
                (UUID) rs.getObject("id"), rs.getString("numero_dossier"), rs.getString("intitule"),
                localDate(rs.getDate("date_ouverture")), rs.getString("statut"), rs.getString("role"),
                rs.getString("fonction_occupee"), rs.getString("entite_libelle_a_l_epoque"),
                localDate(rs.getDate("date_debut")), localDate(rs.getDate("date_fin")),
                faitsParImplication.getOrDefault((UUID) rs.getObject("implication_id"), List.of())), personneId);

        return new Fiche(personneId, (String) identite.get("numero"), (String) identite.get("nom"),
                (String) identite.get("type"), (LocalDate) identite.get("naissance"),
                (String) identite.get("nationalite"), (String) identite.get("profession"),
                (String) identite.get("forme"), (String) identite.get("sigle"), dossiers);
    }

    /** Relaxe, acquittement, non-lieu, classement : l'issue est favorable a la personne. */
    static boolean estIssueFavorable(String statut) {
        if (statut == null) return false;
        String s = statut.toLowerCase(Locale.ROOT);
        return s.contains("relaxe") || s.contains("acquitt") || s.contains("non-lieu") || s.contains("non lieu")
                || s.contains("classement");
    }

    private static LocalDate localDate(Date d) { return d == null ? null : d.toLocalDate(); }

    private static boolean rempli(String s) { return s != null && !s.isBlank(); }

    private static int longueur(String s) { return s == null ? 0 : s.trim().length(); }
}
