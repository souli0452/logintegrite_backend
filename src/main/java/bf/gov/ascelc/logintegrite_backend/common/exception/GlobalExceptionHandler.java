// common/exception/GlobalExceptionHandler.java
package bf.gov.ascelc.logintegrite_backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Traduction centralisee des erreurs en ProblemDetail (RFC 9457).
 * Herite de ResponseEntityExceptionHandler pour couvrir les exceptions MVC standard
 * (route inconnue -> 404, methode non supportee -> 405, corps illisible -> 400,
 * fichier trop volumineux -> 413...). Aucune erreur inattendue ne divulgue de detail interne.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** ProblemDetail standard ; "message" reprend "detail" pour les clients qui lisent error.message. */
    private static ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(statut, detail);
        pd.setTitle(titre);
        pd.setProperty("message", detail);
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, FichierInvalideException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Requete invalide", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return probleme(HttpStatus.FORBIDDEN, "Acces refuse", "Privileges insuffisants pour cette operation");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return probleme(HttpStatus.UNAUTHORIZED, "Non authentifie", "Authentification requise");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        String reference = UUID.randomUUID().toString();
        log.error("Erreur interne non geree [ref={}]", reference, ex);
        ProblemDetail pd = probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", "Une erreur interne est survenue");
        pd.setProperty("reference", reference);
        return pd;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Erreurs de validation");
        pd.setTitle("Requete invalide");
        Map<String, String> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        e -> e.getDefaultMessage() == null ? "invalide" : e.getDefaultMessage(), (a, b) -> a));
        pd.setProperty("erreurs", erreurs);
        return ResponseEntity.badRequest().body(pd);
    }
}
