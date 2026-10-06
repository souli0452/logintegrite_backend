package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EtatTrimestrielServiceTest {

    private final RapportService rapports = mock(RapportService.class);
    private final EtatTrimestrielRepository repo = mock(EtatTrimestrielRepository.class);
    private final EtatTrimestrielService service = new EtatTrimestrielService(rapports, repo);
    private static final PeriodeTrimestre T3 = new PeriodeTrimestre(2026, 3);

    @Test
    void genereEtArchiveAvecEmpreintesEtSynthese() {
        UUID a = UUID.randomUUID();
        when(rapports.lireRegistre()).thenReturn(List.of(new LigneRegistre(a, "Alice", "PHYSIQUE", 1)));
        when(repo.idsDernierEtatAvant(T3)).thenReturn(null);
        when(repo.compterChangementsStatut(T3.debut(), T3.fin())).thenReturn(2L);
        when(repo.compterDossiersClos(T3.debut(), T3.fin())).thenReturn(1L);

        service.generer(T3, "SYSTEME", LocalDate.of(2026, 10, 6));

        ArgumentCaptor<byte[]> pdf = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> sha = ArgumentCaptor.forClass(String.class);
        verify(repo).archiver(eq(T3), eq("SYSTEME"), eq(List.of(a)), pdf.capture(), any(), sha.capture(), anyString());
        assertThat(pdf.getValue()).isNotEmpty();
        assertThat(sha.getValue()).matches("[0-9a-f]{64}");
    }

    @Test
    void registreVideEstArchiveSansErreur() {
        when(rapports.lireRegistre()).thenReturn(List.of());
        service.generer(T3, "SYSTEME", LocalDate.of(2026, 10, 6));
        verify(repo).archiver(eq(T3), eq("SYSTEME"), eq(List.of()), any(), any(), anyString(), anyString());
    }

    @Test
    void erreurDeLectureNArchiveRien() {
        when(rapports.lireRegistre()).thenThrow(new IllegalArgumentException("Export limite"));
        assertThatThrownBy(() -> service.generer(T3, "SYSTEME", LocalDate.of(2026, 10, 6)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).archiver(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void genererSiManquantCibleLeTrimestrePrecedent() {
        when(repo.existeActif(T3)).thenReturn(false);
        when(rapports.lireRegistre()).thenReturn(List.of());
        assertThat(service.genererSiManquant(LocalDate.of(2026, 10, 6))).isTrue();
        verify(repo).archiver(eq(T3), eq("SYSTEME"), any(), any(), any(), anyString(), anyString());
    }

    @Test
    void genererSiManquantEstIdempotent() {
        when(repo.existeActif(T3)).thenReturn(true);
        assertThat(service.genererSiManquant(LocalDate.of(2026, 10, 6))).isFalse();
        verify(repo, never()).archiver(any(), any(), any(), any(), any(), any(), any());
    }
}
