package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EtatTrimestrielRepository {

    private final JdbcTemplate jdbc;

    public record EtatTrimestrielResume(UUID id, int annee, int trimestre, LocalDate dateArret,
                                        Instant dateGeneration, String generePar, int nbPersonnes,
                                        String sha256Pdf, String sha256Excel, boolean remplace) { }

    public boolean existeActif(PeriodeTrimestre p) {
        Integer n = jdbc.queryForObject(
                "select count(*) from audit.etat_trimestriel where annee = ? and trimestre = ? and not remplace",
                Integer.class, p.annee(), p.trimestre());
        return n != null && n > 0;
    }

    /** Identifiants du registre au dernier etat actif strictement anterieur a la periode ; null s'il n'y en a pas. */
    public Set<UUID> idsDernierEtatAvant(PeriodeTrimestre p) {
        List<Set<UUID>> r = jdbc.query(
                "select personne_ids from audit.etat_trimestriel where not remplace "
                        + "and (annee < ? or (annee = ? and trimestre < ?)) order by annee desc, trimestre desc limit 1",
                (rs, i) -> {
                    Array a = rs.getArray(1);
                    Set<UUID> ids = new HashSet<>();
                    for (Object o : (Object[]) a.getArray()) ids.add((UUID) o);
                    return ids;
                }, p.annee(), p.annee(), p.trimestre());
        return r.isEmpty() ? null : r.get(0);
    }

    public long compterChangementsStatut(LocalDate debut, LocalDate fin) {
        Long n = jdbc.queryForObject(
                "select count(*) from audit.journal_audit where action = 'MODIFICATION_STATUT_JUDICIAIRE' "
                        + "and entite_cible = 'ImplicationFait' and date_action >= ?::date "
                        + "and date_action < (?::date + 1)",
                Long.class, debut, fin);
        return n == null ? 0 : n;
    }

    public long compterDossiersClos(LocalDate debut, LocalDate fin) {
        Long n = jdbc.queryForObject(
                "select count(*) from dossiers.dossier where date_cloture between ? and ?", Long.class, debut, fin);
        return n == null ? 0 : n;
    }

    @Transactional
    public void archiver(PeriodeTrimestre p, String generePar, List<UUID> ids, byte[] pdf, byte[] excel,
                         String shaPdf, String shaExcel) {
        jdbc.update("update audit.etat_trimestriel set remplace = true where annee = ? and trimestre = ? and not remplace",
                p.annee(), p.trimestre());
        jdbc.update("insert into audit.etat_trimestriel (annee, trimestre, date_arret, genere_par, nb_personnes, "
                        + "personne_ids, pdf, excel, sha256_pdf, sha256_excel) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, p.annee());
                    ps.setInt(2, p.trimestre());
                    ps.setObject(3, p.fin());
                    ps.setString(4, generePar);
                    ps.setInt(5, ids.size());
                    ps.setArray(6, ps.getConnection().createArrayOf("uuid", ids.toArray()));
                    ps.setBytes(7, pdf);
                    ps.setBytes(8, excel);
                    ps.setString(9, shaPdf);
                    ps.setString(10, shaExcel);
                });
    }

    public List<EtatTrimestrielResume> lister() {
        return jdbc.query("select id, annee, trimestre, date_arret, date_generation, genere_par, nb_personnes, "
                        + "sha256_pdf, sha256_excel, remplace from audit.etat_trimestriel "
                        + "order by annee desc, trimestre desc, date_generation desc",
                (rs, i) -> new EtatTrimestrielResume(rs.getObject("id", UUID.class), rs.getInt("annee"),
                        rs.getInt("trimestre"), rs.getObject("date_arret", LocalDate.class),
                        rs.getTimestamp("date_generation").toInstant(), rs.getString("genere_par"),
                        rs.getInt("nb_personnes"), rs.getString("sha256_pdf"), rs.getString("sha256_excel"),
                        rs.getBoolean("remplace")));
    }

    public Optional<byte[]> pdf(UUID id) { return octets("pdf", id); }

    public Optional<byte[]> excel(UUID id) { return octets("excel", id); }

    private Optional<byte[]> octets(String colonne, UUID id) {
        List<byte[]> r = jdbc.query("select " + colonne + " from audit.etat_trimestriel where id = ?",
                (rs, i) -> rs.getBytes(1), id);
        return r.stream().findFirst();
    }
}
