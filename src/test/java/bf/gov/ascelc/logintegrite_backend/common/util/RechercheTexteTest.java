package bf.gov.ascelc.logintegrite_backend.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RechercheTexteTest {

    @Test
    void absentOuBlancDonneUneChaineVide() {
        assertThat(RechercheTexte.normaliser(null)).isEmpty();
        assertThat(RechercheTexte.normaliser("   ")).isEmpty();
        assertThat(RechercheTexte.echapperLike(null)).isEmpty();
    }

    @Test
    void retireLesEspacesAutourDuTerme() {
        assertThat(RechercheTexte.normaliser("  DOSS-2026  ")).isEqualTo("DOSS-2026");
    }

    @Test
    void neutraliseLesJokersLike() {
        // "%" ou "_" saisis par l'utilisateur doivent etre cherches tels quels, pas devenir des jokers
        assertThat(RechercheTexte.echapperLike("100%")).isEqualTo("100\\%");
        assertThat(RechercheTexte.echapperLike("a_b")).isEqualTo("a\\_b");
        assertThat(RechercheTexte.echapperLike("a\\b")).isEqualTo("a\\\\b");
    }
}
