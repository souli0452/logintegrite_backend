package bf.gov.ascelc.logintegrite_backend.common.exception;

/** Fichier depose refuse (extension, contenu ou taille non conformes). Traduit en HTTP 400. */
public class FichierInvalideException extends RuntimeException {
    public FichierInvalideException(String message) {
        super(message);
    }
}
