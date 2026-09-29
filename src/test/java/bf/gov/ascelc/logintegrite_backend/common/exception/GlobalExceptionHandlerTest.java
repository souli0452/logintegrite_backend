package bf.gov.ascelc.logintegrite_backend.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void ressourceIntrouvableDonne404AvecMessage() {
        ProblemDetail pd = handler.handleNotFound(new ResourceNotFoundException("Dossier", UUID.randomUUID()));

        assertThat(pd.getStatus()).isEqualTo(404);
        assertThat(pd.getProperties()).containsKey("message");
        assertThat(pd.getDetail()).contains("Dossier introuvable");
    }

    @Test
    void fichierInvalideDonne400() {
        ProblemDetail pd = handler.handleBadRequest(new FichierInvalideException("Extension non autorisee"));

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getDetail()).isEqualTo("Extension non autorisee");
    }

    @Test
    void accesRefuseDonne403SansDetailInterne() {
        ProblemDetail pd = handler.handleAccessDenied(new AccessDeniedException("expression SpEL interne"));

        assertThat(pd.getStatus()).isEqualTo(403);
        assertThat(pd.getDetail()).doesNotContain("SpEL");
    }

    @Test
    void erreurInattendueNeDivulguePasLeMessageInterneEtDonneUneReference() {
        ProblemDetail pd = handler.handleUnexpected(new IllegalStateException("password=secret jdbc://interne"));

        assertThat(pd.getStatus()).isEqualTo(500);
        assertThat(pd.getDetail()).doesNotContain("secret").doesNotContain("jdbc");
        assertThat(pd.getProperties()).containsKey("reference");
    }
}
