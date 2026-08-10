package bf.gov.ascelc.logintegrite_backend.common.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entite, UUID id) {
        super(entite + " introuvable pour l'id " + id);
    }
}
