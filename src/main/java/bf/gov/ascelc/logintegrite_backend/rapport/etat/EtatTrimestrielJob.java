package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Verifie chaque jour que l'etat du trimestre ecoule existe, et le genere sinon. Couvre a la fois la generation
 * normale (le 1er jour du trimestre suivant) et le rattrapage apres une indisponibilite du serveur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EtatTrimestrielJob {

    private final EtatTrimestrielService service;

    @Scheduled(initialDelay = 180_000, fixedDelay = 86_400_000)
    public void verifier() {
        try {
            service.genererSiManquant(LocalDate.now());
        } catch (RuntimeException e) {
            // Reessaye au prochain passage ; ne bloque jamais le demarrage.
            log.warn("Generation de l'etat trimestriel impossible : {}", e.getMessage());
        }
    }
}
