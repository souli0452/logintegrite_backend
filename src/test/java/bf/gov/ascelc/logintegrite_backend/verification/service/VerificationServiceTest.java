package bf.gov.ascelc.logintegrite_backend.verification.service;

import bf.gov.ascelc.logintegrite_backend.verification.dto.VerificationDtos.Criteres;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationServiceTest {

    private static Criteres c(String piece, String rccm, String ifu, String numero, String nom, String prenoms, LocalDate naissance) {
        return new Criteres(piece, rccm, ifu, numero, nom, prenoms, naissance);
    }

    @Test
    void refuse_une_recherche_sans_aucun_critere() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> VerificationService.verifierCriteres(c(null, null, null, null, null, null, null)));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    void refuse_un_nom_seul_ou_un_nom_sans_date_de_naissance() {
        assertThrows(ResponseStatusException.class,
                () -> VerificationService.verifierCriteres(c(null, null, null, null, "Ouedraogo", null, null)));
        assertThrows(ResponseStatusException.class,
                () -> VerificationService.verifierCriteres(c(null, null, null, null, "Ouedraogo", "Salif", null)));
        assertThrows(ResponseStatusException.class,
                () -> VerificationService.verifierCriteres(c(null, null, null, null, null, null, LocalDate.of(1980, 1, 1))));
    }

    @Test
    void refuse_un_numero_trop_court_pour_identifier_quelqu_un() {
        assertThrows(ResponseStatusException.class,
                () -> VerificationService.verifierCriteres(c("123", null, null, null, null, null, null)));
    }

    @Test
    void accepte_un_numero_precis_ou_une_identite_complete() {
        assertDoesNotThrow(() -> VerificationService.verifierCriteres(c("B1234567", null, null, null, null, null, null)));
        assertDoesNotThrow(() -> VerificationService.verifierCriteres(c(null, "BF-OUA-2020-B-1234", null, null, null, null, null)));
        assertDoesNotThrow(() -> VerificationService.verifierCriteres(c(null, null, "00012345A", null, null, null, null)));
        assertDoesNotThrow(() -> VerificationService.verifierCriteres(c(null, null, null, "PERS-2026-00040", null, null, null)));
        assertDoesNotThrow(() -> VerificationService.verifierCriteres(
                c(null, null, null, null, "Ouedraogo", "Salif", LocalDate.of(1980, 5, 12))));
    }

    @Test
    void reconnait_les_issues_favorables() {
        assertTrue(VerificationService.estIssueFavorable("Relaxe ou acquittement"));
        assertTrue(VerificationService.estIssueFavorable("Non-lieu"));
        assertTrue(VerificationService.estIssueFavorable("Classement sans suite"));
        assertFalse(VerificationService.estIssueFavorable("Condamnation définitive"));
        assertFalse(VerificationService.estIssueFavorable("Mise en cause"));
        assertFalse(VerificationService.estIssueFavorable(null));
    }
}
