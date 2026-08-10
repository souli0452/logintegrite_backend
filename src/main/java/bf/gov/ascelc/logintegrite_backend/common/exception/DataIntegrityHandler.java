// common/exception/DataIntegrityHandler.java
package bf.gov.ascelc.logintegrite_backend.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class DataIntegrityHandler {

    private static final Logger log = LoggerFactory.getLogger(DataIntegrityHandler.class);

    // Doublon UNIQUE : Key (libelle)=(Complice) already exists.
    private static final Pattern PATTERN_DOUBLON =
        Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\) already exists");

    // NOT NULL : null value in column "numero_dossier" of relation "dossier" violates not-null constraint
    private static final Pattern PATTERN_NOT_NULL =
        Pattern.compile("null value in column \"([^\"]+)\".*violates not-null");

    // FK : insert or update on table "..." violates foreign key constraint "fk_xxx"
    //      Key (source_signalement_id)=(...) is not present in table "source_signalement".
    private static final Pattern PATTERN_FK =
        Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]+)\\) is not present in table \"([^\"]+)\"");

    // CHECK : new row for relation "..." violates check constraint "chk_xxx"
    private static final Pattern PATTERN_CHECK =
        Pattern.compile("violates check constraint \"([^\"]+)\"");

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> gerer(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        String texte = cause != null ? cause : "";

        // Log complet pour debug côté serveur (essentiel pour tracer les cas intermittents)
        log.warn("Violation d'integrite: {}", texte);

        // 1. Doublon UNIQUE → 409 Conflict (vraie sémantique de conflit d'existence)
        Matcher m = PATTERN_DOUBLON.matcher(texte);
        if (m.find()) {
            return reponse(HttpStatus.CONFLICT, "Conflict", String.format(
                "La valeur '%s' existe deja pour le champ '%s'.",
                m.group(2), m.group(1)));
        }

        // 2. NOT NULL manquant → 400 Bad Request
        m = PATTERN_NOT_NULL.matcher(texte);
        if (m.find()) {
            return reponse(HttpStatus.BAD_REQUEST, "Bad Request", String.format(
                "Le champ '%s' est obligatoire mais n'a pas ete fourni.",
                m.group(1)));
        }

        // 3. Foreign key invalide → 400 Bad Request
        m = PATTERN_FK.matcher(texte);
        if (m.find()) {
            return reponse(HttpStatus.BAD_REQUEST, "Bad Request", String.format(
                "La reference '%s' pointe vers un enregistrement inexistant dans '%s'.",
                m.group(1), m.group(3)));
        }

        // 4. Check constraint → 400 Bad Request
        m = PATTERN_CHECK.matcher(texte);
        if (m.find()) {
            return reponse(HttpStatus.BAD_REQUEST, "Bad Request", String.format(
                "La contrainte de coherence '%s' n'est pas respectee.",
                m.group(1)));
        }

        // 5. Troncature de chaine (varchar trop court) → 400 Bad Request
        if (texte.contains("value too long for type")) {
            return reponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "Un des champs saisis depasse la longueur maximale autorisee. "
                + "Reduisez le texte et reessayez.");
        }

        // 6. Fallback : 409 Conflict avec un extrait pour le debug
        String extrait = texte.length() > 200 ? texte.substring(0, 200) + "..." : texte;
        return reponse(HttpStatus.CONFLICT, "Conflict", "Violation d'integrite: " + extrait);
    }

    private ResponseEntity<Map<String, Object>> reponse(HttpStatus statut, String erreur, String message) {
        return ResponseEntity.status(statut).body(Map.of(
            "status", statut.value(),
            "error", erreur,
            "message", message
        ));
    }
}
