package bf.gov.ascelc.logintegrite_backend.verification.service;

import bf.gov.ascelc.logintegrite_backend.audit.service.JournalSecuriteService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Demande;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Demandes d'export d'un dossier complet. Un consultant consulte a l'ecran ; pour OBTENIR un export, il dépose une
 * demande motivee, qu'un administrateur accorde ou refuse. Chaque etape est journalisee et rien n'est supprime.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DemandeExportService {

    static final int MOTIF_MIN = 20;
    static final int MOTIF_MAX = 1000;

    private static final String SELECT = """
            select d.id, d.date_demande, d.personne_id, p.nom_affichage as personne, u.prenom || ' ' || u.nom as demandeur,
                   d.motif, d.statut, t.prenom || ' ' || t.nom as traite_par, d.date_traitement, d.commentaire
            from audit.demande_export d
            join personnes.personne p on p.id = d.personne_id
            join securite.utilisateur u on u.id = d.demandeur_id
            left join securite.utilisateur t on t.id = d.traite_par_id
            """;

    private final JdbcTemplate jdbc;
    private final CurrentUserProvider currentUserProvider;
    private final VerificationService verification;
    private final JournalSecuriteService journal;

    public Demande creer(UUID personneId, String motif) {
        String texte = motif == null ? "" : motif.trim();
        if (texte.length() < MOTIF_MIN || texte.length() > MOTIF_MAX) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expliquez le motif de la demande (entre " + MOTIF_MIN + " et " + MOTIF_MAX + " caracteres).");
        }
        // Seule une personne visible par le consultant (registre officiel) peut faire l'objet d'une demande.
        verification.obtenirFiche(personneId);

        UUID demandeur = currentUserProvider.utilisateurCourant().getId();
        UUID id = UUID.randomUUID();
        try {
            jdbc.update("insert into audit.demande_export (id, demandeur_id, personne_id, motif) values (?, ?, ?, ?)",
                    id, demandeur, personneId, texte);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une demande est deja en attente pour cette personne.");
        }
        journal.enregistrerEvenementServeur("DEMANDE_EXPORT", "/verification/personnes/" + personneId, texte);
        return obtenir(id);
    }

    @Transactional(readOnly = true)
    public List<Demande> mesDemandes() {
        return jdbc.query(SELECT + " where d.demandeur_id = ? order by d.date_demande desc",
                this::ligne, currentUserProvider.utilisateurCourant().getId());
    }

    @Transactional(readOnly = true)
    public List<Demande> lister(String statut) {
        if (statut == null || statut.isBlank()) {
            return jdbc.query(SELECT + " order by (d.statut = 'EN_ATTENTE') desc, d.date_demande desc", this::ligne);
        }
        return jdbc.query(SELECT + " where d.statut = ? order by d.date_demande desc", this::ligne, statut);
    }

    public Demande decider(UUID id, String decision, String commentaire) {
        String statut = "ACCORDEE".equals(decision) ? "ACCORDEE" : "REFUSEE".equals(decision) ? "REFUSEE" : null;
        if (statut == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision attendue : ACCORDEE ou REFUSEE.");
        }
        String note = commentaire == null ? "" : commentaire.trim();
        if ("REFUSEE".equals(statut) && note.length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indiquez la raison du refus.");
        }
        int modifiees = jdbc.update("""
                update audit.demande_export
                   set statut = ?, traite_par_id = ?, date_traitement = now(), commentaire = ?
                 where id = ? and statut = 'EN_ATTENTE'
                """, statut, currentUserProvider.utilisateurCourant().getId(), note.isEmpty() ? null : note, id);
        if (modifiees == 0) {
            // soit elle n'existe pas, soit elle a deja ete traitee
            obtenir(id);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette demande a deja ete traitee.");
        }
        journal.enregistrerEvenementServeur("DEMANDE_EXPORT_" + statut, "/demandes-export/" + id, note);
        return obtenir(id);
    }

    private Demande obtenir(UUID id) {
        List<Demande> l = jdbc.query(SELECT + " where d.id = ?", this::ligne, id);
        if (l.isEmpty()) throw new ResourceNotFoundException("Demande", id);
        return l.get(0);
    }

    private Demande ligne(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        Timestamp traitement = rs.getTimestamp("date_traitement");
        return new Demande((UUID) rs.getObject("id"), rs.getTimestamp("date_demande").toInstant(),
                (UUID) rs.getObject("personne_id"), rs.getString("personne"), rs.getString("demandeur"),
                rs.getString("motif"), rs.getString("statut"), rs.getString("traite_par"),
                traitement == null ? null : traitement.toInstant(), rs.getString("commentaire"));
    }
}
