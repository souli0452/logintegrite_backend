package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class SyntheseTrimestreTest {

    private static LigneRegistre ligne(UUID id, String nom) {
        return new LigneRegistre(id, nom, "PHYSIQUE", 1);
    }

    @Test
    void premierEtatNeCalculePasDeDifference() {
        SyntheseTrimestre s = SyntheseTrimestre.calculer(null, List.of(ligne(UUID.randomUUID(), "A")), 3, 2);
        assertThat(s.premierEtat()).isTrue();
        assertThat(s.nouveaux()).isEmpty();
        assertThat(s.sortis()).isEmpty();
        assertThat(s.changementsStatut()).isEqualTo(3);
        assertThat(s.dossiersClos()).isEqualTo(2);
    }

    @Test
    void differenceAvecLEtatPrecedent() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        SyntheseTrimestre s = SyntheseTrimestre.calculer(Set.of(a, b),
                List.of(ligne(b, "B"), ligne(c, "C")), 0, 0);
        assertThat(s.premierEtat()).isFalse();
        assertThat(s.nouveaux()).containsExactly("C");
        assertThat(s.sortis()).containsExactly(a.toString());
    }

    @Test
    void registreVideSansPrecedent() {
        SyntheseTrimestre s = SyntheseTrimestre.calculer(Set.of(), List.of(), 0, 0);
        assertThat(s.nouveaux()).isEmpty();
        assertThat(s.sortis()).isEmpty();
    }
}
