// common/exception/DataIntegrityHandler.java
package bf.gov.ascelc.logintegrite_backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduit les violations de contraintes PostgreSQL en reponses HTTP lisibles, au meme format
 * (ProblemDetail + propriete "message") que le reste de l'API. Le texte brut de l'erreur SQL
 * (noms de tables, de contraintes) n'est jamais renvoye au client : il reste dans les logs.
 */
@RestControllerAdvice
public class DataIntegrityHandler {

    private static final Logger log = LoggerFactory.getLogger(DataIntegrityHandler.class);

    // Doublon UNIQUE : Key (libelle)=(Complice) already exists.
    private static final Pattern PATTERN_DOUBLON =
        Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\) already exists");

    // NOT NULL : null value in column "numero_dossier" of relation "dossier" violates not-null constraint
    private static final Pattern PATTERN_NOT_NULL =
        Pattern.compile("null value in column \"([^\"]+)\".*violates not-null");

    // FK : Key (source_signalement_id)=(...) is not present in table "source_signalement".
    private static final Pattern PATTERN_FK =
        Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\) is not present in table \"([^\"]+)\"");

    // CHECK : new row for relation "..." violates check constraint "chk_xxx"
    private static final Pattern PATTERN_CHECK =
        Pattern.compile("violates check constraint \"([^\"]+)\"");

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail gerer(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        String texte = cause != null ? cause : "";

        // 1. Doublon UNIQUE -> 409 Conflict
        Matcher m = PATTERN_DOUBLON.matcher(texte);
        if (m.find()) {
            return probleme(HttpStatus.CONFLICT, "Conflit de donnees", String.format(
                "La valeur '%s' existe deja pour le champ '%s'.", m.group(2), m.group(1)));
        }

        // 2. NOT NULL manquant -> 400
        m = PATTERN_NOT_NULL.matcher(texte);
        if (m.find()) {
            return probleme(HttpStatus.BAD_REQUEST, "Requete invalide", String.format(
                "Le champ '%s' est obligatoire mais n'a pas ete fourni.", m.group(1)));
        }

        // 3. Foreign key invalide -> 400
        m = PATTERN_FK.matcher(texte);
        if (m.find()) {
            return probleme(HttpStatus.BAD_REQUEST, "Requete invalide", String.format(
                "La reference '%s' pointe vers un enregistrement inexistant.", m.group(1)));
        }

        // 4. Check constraint -> 400
        m = PATTERN_CHECK.matcher(texte);
        if (m.find()) {
            return probleme(HttpStatus.BAD_REQUEST, "Requete invalide",
                "Une regle de coherence des donnees n'est pas respectee.");
        }

        // 5. Troncature de chaine (varchar trop court) -> 400
        if (texte.contains("value too long for type")) {
            return probleme(HttpStatus.BAD_REQUEST, "Requete invalide",
                "Un des champs saisis depasse la longueur maximale autorisee. Reduisez le texte et reessayez.");
        }

        // 6. Cas non prevu : detail technique uniquement dans les logs, identifiant de reference pour le support.
        String reference = UUID.randomUUID().toString();
        log.warn("Violation d'integrite non reconnue [ref={}] : {}", reference, texte);
        ProblemDetail pd = probleme(HttpStatus.CONFLICT, "Conflit de donnees",
            "L'operation viole une contrainte d'integrite des donnees.");
        pd.setProperty("reference", reference);
        return pd;
    }

    /** ProblemDetail standard ; "message" reprend "detail" pour les clients qui lisent error.message. */
    private static ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(statut, detail);
        pd.setTitle(titre);
        pd.setProperty("message", detail);
        return pd;
    }
}
