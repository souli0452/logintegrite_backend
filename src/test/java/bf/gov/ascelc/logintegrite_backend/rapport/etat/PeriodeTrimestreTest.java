package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodeTrimestreTest {

    @Test
    void deduitLeTrimestreDUneDate() {
        assertThat(PeriodeTrimestre.de(LocalDate.of(2026, 1, 1))).isEqualTo(new PeriodeTrimestre(2026, 1));
        assertThat(PeriodeTrimestre.de(LocalDate.of(2026, 3, 31))).isEqualTo(new PeriodeTrimestre(2026, 1));
        assertThat(PeriodeTrimestre.de(LocalDate.of(2026, 10, 6))).isEqualTo(new PeriodeTrimestre(2026, 4));
    }

    @Test
    void bornesDuTrimestre() {
        PeriodeTrimestre t3 = new PeriodeTrimestre(2026, 3);
        assertThat(t3.debut()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(t3.fin()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(new PeriodeTrimestre(2024, 1).fin()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(new PeriodeTrimestre(2026, 4).fin()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void precedentTraverseLeChangementDAnnee() {
        assertThat(new PeriodeTrimestre(2026, 1).precedent()).isEqualTo(new PeriodeTrimestre(2025, 4));
        assertThat(new PeriodeTrimestre(2026, 3).precedent()).isEqualTo(new PeriodeTrimestre(2026, 2));
    }

    @Test
    void libelleEtValidation() {
        assertThat(new PeriodeTrimestre(2026, 3).libelle()).isEqualTo("T3 2026");
        assertThatThrownBy(() -> new PeriodeTrimestre(2026, 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PeriodeTrimestre(2026, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
