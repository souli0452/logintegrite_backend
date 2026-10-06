package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import java.time.LocalDate;

/** Un trimestre civil (T1 = janvier-mars ... T4 = octobre-decembre). */
public record PeriodeTrimestre(int annee, int trimestre) {

    public PeriodeTrimestre {
        if (trimestre < 1 || trimestre > 4) {
            throw new IllegalArgumentException("Trimestre invalide : " + trimestre);
        }
    }

    public static PeriodeTrimestre de(LocalDate date) {
        return new PeriodeTrimestre(date.getYear(), (date.getMonthValue() - 1) / 3 + 1);
    }

    public LocalDate debut() {
        return LocalDate.of(annee, (trimestre - 1) * 3 + 1, 1);
    }

    /** Dernier jour du trimestre : la date d'arret de l'etat. */
    public LocalDate fin() {
        return debut().plusMonths(3).minusDays(1);
    }

    public PeriodeTrimestre precedent() {
        return trimestre == 1 ? new PeriodeTrimestre(annee - 1, 4) : new PeriodeTrimestre(annee, trimestre - 1);
    }

    public String libelle() {
        return "T" + trimestre + " " + annee;
    }
}
