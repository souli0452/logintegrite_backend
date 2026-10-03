package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Regle commune : un dossier cloture n'est plus modifiable (ni son contenu, ni ses personnes, ni ses faits, ni ses documents).
 * Les decisions de justice (statuts judiciaires et peines) peuvent, elles, toujours etre enregistrees apres la cloture :
 * elles sont souvent posterieures a l'instruction.
 */
public final class RegleDossier {

    private RegleDossier() { }

    public static void verifierOuvert(Dossier dossier) {
        if (dossier.getStatutDossier() != StatutDossier.OUVERT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le dossier est cloture : il n'est plus modifiable.");
        }
    }
}
