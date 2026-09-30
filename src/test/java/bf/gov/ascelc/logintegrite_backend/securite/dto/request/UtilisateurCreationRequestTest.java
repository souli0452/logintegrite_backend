package bf.gov.ascelc.logintegrite_backend.securite.dto.request;

import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static UtilisateurCreationRequest avecMotDePasse(String motDePasse) {
        UtilisateurCreationRequest r = new UtilisateurCreationRequest();
        r.setNom("Ouedraogo");
        r.setPrenom("Salif");
        r.setEmail("salif@asce-lc.bf");
        r.setRoleInitial(CodeRole.AGENT);
        r.setMotDePasseTemporaire(motDePasse);
        return r;
    }

    private Set<String> messages(String motDePasse) {
        return validator.validate(avecMotDePasse(motDePasse)).stream()
                .map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    @Test
    void refuseUnMotDePasseDeMoinsDeDouzeCaracteres() {
        assertThat(messages("Abcdefg1234")).anyMatch(m -> m.contains("au moins 12"));   // 11 caracteres
        assertThat(messages("Abcd1234")).anyMatch(m -> m.contains("au moins 12"));      // ancienne regle : 8
    }

    @Test
    void accepteExactementDouzeCaracteresConformes() {
        assertThat(messages("Abcdefgh1234")).isEmpty();
    }

    @Test
    void refuseUnMotDePasseLongSansMajusculeMinusculeOuChiffre() {
        assertThat(messages("abcdefghijkl1")).anyMatch(m -> m.contains("majuscule"));
        assertThat(messages("ABCDEFGHIJKL1")).anyMatch(m -> m.contains("minuscule"));
        assertThat(messages("Abcdefghijklm")).anyMatch(m -> m.contains("chiffre"));
    }

    @Test
    void refuseUnMotDePasseAbsentOuVide() {
        assertThat(messages(null)).isNotEmpty();
        assertThat(messages("")).isNotEmpty();
    }
}
