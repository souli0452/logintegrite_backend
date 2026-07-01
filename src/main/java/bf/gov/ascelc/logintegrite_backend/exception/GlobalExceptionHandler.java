package bf.gov.ascelc.logintegrite_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erreur", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalStateException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("erreur", ex.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationEchouee(MethodArgumentNotValidException ex) {
        Map<String, String> erreursParChamp = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erreursParChamp.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> corps = new HashMap<>();
        corps.put("erreur", "La validation des données a échoué.");
        corps.put("champs", erreursParChamp);
        return ResponseEntity.badRequest().body(corps);
    }

    // Accès refusé par une annotation @PreAuthorize
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> accesRefusePreAuthorize(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erreur", "Accès refusé : privilèges insuffisants pour cette action."));
    }

    // Accès refusé levé manuellement dans le code (contrôle de propriété / IDOR)
    // Utilisé par RechercheSauvegardeeServiceImpl et NotificationServiceImpl
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accesRefuseManuel(AccessDeniedException ex) {
        log.warn("Accès refusé (contrôle de propriété) : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erreur", "Accès refusé : vous n'êtes pas autorisé à accéder à cette ressource."));
    }

    // Handler générique : ne renvoie plus ex.getMessage() brut au client,
    // et journalise désormais l'erreur côté serveur avec une référence
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generic(Exception ex) {
        String reference = UUID.randomUUID().toString();
        log.error("Erreur interne non gérée [ref={}]", reference, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "erreur", "Erreur interne. Contactez le support avec la référence ci-dessous.",
                        "reference", reference
                ));
    }
}
