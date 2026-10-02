package bf.gov.ascelc.logintegrite_backend.securite.service;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/**
 * Regles de la date d'expiration d'un compte :
 *  - un compte de consultation a TOUJOURS une date (6 mois par defaut) : on ne laisse pas un acces exterieur ouvert sans fin ;
 *  - les autres comptes (agents, validateurs, administrateurs) peuvent ne pas en avoir ;
 *  - une date ne peut pas etre dans le passe.
 */
public final class RegleExpiration {

    static final int MOIS_PAR_DEFAUT = 6;

    private RegleExpiration() { }

    /** Date a enregistrer a la creation, ou null si le compte n'expire pas. */
    public static LocalDate pourCreation(CodeRole role, LocalDate demandee, LocalDate aujourdhui) {
        if (demandee == null) {
            return role == CodeRole.CONSULTANT ? aujourdhui.plusMonths(MOIS_PAR_DEFAUT) : null;
        }
        verifierFutur(demandee, aujourdhui);
        return demandee;
    }

    /** Controle d'une modification demandee par un administrateur. */
    public static void verifierModification(boolean consultationSeule, LocalDate nouvelle, LocalDate aujourdhui) {
        if (nouvelle == null) {
            if (consultationSeule) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Un compte de consultation doit avoir une date d'expiration.");
            }
            return;
        }
        verifierFutur(nouvelle, aujourdhui);
    }

    private static void verifierFutur(LocalDate date, LocalDate aujourdhui) {
        if (date.isBefore(aujourdhui)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date d'expiration ne peut pas etre dans le passe.");
        }
    }
}
