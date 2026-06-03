package bf.gov.ascelc.logintegrite_backend.exception;

import org.springframework.http.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,String>> notFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("erreur", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> badRequest(
            IllegalStateException ex) {
        return ResponseEntity.badRequest()
            .body(Map.of("erreur", ex.getMessage()));
    }

    // ── Interception des accès refusés par @PreAuthorize (HTTP 403) ──
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String,String>> handleAccessDenied(
            AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("erreur", "Accès refusé : Privilèges insuffisants pour cette action."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> generic(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("erreur", "Erreur interne : " 
                + ex.getMessage()));
    }
}
