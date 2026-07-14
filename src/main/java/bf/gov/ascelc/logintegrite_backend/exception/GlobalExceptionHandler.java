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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erreur", ex.getMessage()));
    }

    // AJOUT : une personne ou une structure est déjà inscrite au registre
    // officiel. On renvoie 409 CONFLICT avec l'ID de la fiche existante pour
    // permettre au frontend de rediriger l'utilisateur vers celle-ci.
    @ExceptionHandler(FicheDejaExistanteException.class)
    public ResponseEntity<Map<String, Object>> ficheDejaExistante(FicheDejaExistanteException ex) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("erreur", ex.getMessage());
        corps.put("ficheExistanteId", ex.getFicheExistanteId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corps);
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

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> accesRefusePreAuthorize(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erreur", "Accès refusé : privilèges insuffisants pour cette action."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accesRefuseManuel(AccessDeniedException ex) {
        log.warn("Accès refusé (contrôle de propriété) : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erreur", "Accès refusé : vous n'êtes pas autorisé à accéder à cette ressource."));
    }

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
    
    @ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<Map<String, Object>> validationParametresEchouee(ConstraintViolationException ex) {
    Map<String, String> erreursParChamp = new HashMap<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
        String chemin = violation.getPropertyPath().toString();
        String cle = chemin.contains(".") ? chemin.substring(chemin.lastIndexOf('.') + 1) : chemin;
        erreursParChamp.put(cle, violation.getMessage());
    }
    Map<String, Object> corps = new HashMap<>();
    corps.put("erreur", "La validation des paramètres a échoué.");
    corps.put("champs", erreursParChamp);
    return ResponseEntity.badRequest().body(corps);
}
}
