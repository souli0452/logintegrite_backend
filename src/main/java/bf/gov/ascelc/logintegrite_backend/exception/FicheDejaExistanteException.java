package bf.gov.ascelc.logintegrite_backend.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FicheDejaExistanteException extends RuntimeException {

    private final UUID ficheExistanteId;

    public FicheDejaExistanteException(String message, UUID ficheExistanteId) {
        super(message);
        this.ficheExistanteId = ficheExistanteId;
    }
}