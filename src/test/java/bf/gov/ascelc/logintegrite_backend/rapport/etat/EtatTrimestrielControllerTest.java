package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtatTrimestrielControllerTest {

    private final EtatTrimestrielRepository repo = mock(EtatTrimestrielRepository.class);
    private final EtatTrimestrielService service = mock(EtatTrimestrielService.class);
    private final CurrentUserProvider courant = mock(CurrentUserProvider.class);
    private final EtatTrimestrielController controller = new EtatTrimestrielController(repo, service, courant);

    @Test
    void lectureReserveeAuValidateurEtALAdmin() {
        PreAuthorize p = AnnotatedElementUtils.findMergedAnnotation(EtatTrimestrielController.class, PreAuthorize.class);
        assertThat(p.value()).contains("VALIDATEUR").contains("ADMIN").doesNotContain("AGENT").doesNotContain("CONSULT");
    }

    @Test
    void generationManuelleReserveeALAdmin() throws Exception {
        PreAuthorize p = AnnotatedElementUtils.findMergedAnnotation(
                EtatTrimestrielController.class.getMethod("generer", int.class, int.class), PreAuthorize.class);
        assertThat(p.value()).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    void pdfInconnuDonne404() {
        UUID id = UUID.randomUUID();
        when(repo.pdf(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.pdf(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void pdfConnuEstRenvoye() {
        UUID id = UUID.randomUUID();
        when(repo.pdf(id)).thenReturn(Optional.of(new byte[]{1, 2}));
        assertThat(controller.pdf(id).getBody()).containsExactly(1, 2);
    }

    @Test
    void trimestreNonTermineEstRefuse() {
        int annee = java.time.LocalDate.now().getYear() + 1;
        assertThatThrownBy(() -> controller.generer(annee, 1)).isInstanceOf(IllegalArgumentException.class);
    }
}
