package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EtatTrimestrielJobTest {

    private final EtatTrimestrielService service = mock(EtatTrimestrielService.class);
    private final EtatTrimestrielJob job = new EtatTrimestrielJob(service);

    @Test
    void delegueAuService() {
        job.verifier();
        verify(service).genererSiManquant(any());
    }

    @Test
    void uneErreurNeFaitPasPlanterLaTache() {
        when(service.genererSiManquant(any())).thenThrow(new IllegalStateException("base indisponible"));
        assertThatCode(job::verifier).doesNotThrowAnyException();
    }
}
