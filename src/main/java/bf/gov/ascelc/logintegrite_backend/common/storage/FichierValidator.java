package bf.gov.ascelc.logintegrite_backend.common.storage;

import bf.gov.ascelc.logintegrite_backend.common.exception.FichierInvalideException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * Validation des fichiers deposes : liste blanche d'extensions + verification de la
 * signature binaire (magic bytes). Le type MIME declare par le client n'est jamais
 * fait confiance : on renvoie le type MIME canonique associe a l'extension validee.
 */
@Component
public class FichierValidator {

    /** Resultat d'une validation : extension normalisee et type MIME canonique. */
    public record FichierValide(String extension, String typeMime, String nomOriginal) { }

    private record Type(String mime, byte[]... signatures) { }

    private static final byte[] OLE = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};
    private static final byte[] ZIP = {'P', 'K', 0x03, 0x04};

    private static final Map<String, Type> AUTORISES = Map.of(
            "pdf", new Type("application/pdf", new byte[]{'%', 'P', 'D', 'F'}),
            "jpg", new Type("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            "jpeg", new Type("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
            "png", new Type("image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G'}),
            "doc", new Type("application/msword", OLE),
            "xls", new Type("application/vnd.ms-excel", OLE),
            "docx", new Type("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ZIP),
            "xlsx", new Type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ZIP),
            "txt", new Type("text/plain")
    );

    public FichierValide valider(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new FichierInvalideException("Le fichier est vide ou absent");
        }
        String nom = nettoyerNom(fichier.getOriginalFilename());
        String extension = extension(nom);
        Type type = AUTORISES.get(extension);
        if (type == null) {
            throw new FichierInvalideException(
                    "Extension non autorisee. Extensions acceptees : " + String.join(", ", AUTORISES.keySet().stream().sorted().toList()));
        }
        if (type.signatures().length > 0 && !signatureConforme(fichier, type)) {
            throw new FichierInvalideException("Le contenu du fichier ne correspond pas a l'extension ." + extension);
        }
        return new FichierValide(extension, type.mime(), nom);
    }

    private boolean signatureConforme(MultipartFile fichier, Type type) {
        try (InputStream in = fichier.getInputStream()) {
            byte[] debut = in.readNBytes(8);
            for (byte[] signature : type.signatures()) {
                if (debut.length >= signature.length
                        && Arrays.equals(Arrays.copyOf(debut, signature.length), signature)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new FichierInvalideException("Lecture du fichier impossible");
        }
    }

    /** Retire les composants de chemin et les caracteres de controle du nom fourni par le client. */
    static String nettoyerNom(String brut) {
        if (brut == null || brut.isBlank()) {
            throw new FichierInvalideException("Le nom du fichier est obligatoire");
        }
        String nom = brut.replace('\\', '/');
        nom = nom.substring(nom.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "").trim();
        if (nom.isEmpty() || nom.length() > 200) {
            throw new FichierInvalideException("Nom de fichier invalide");
        }
        return nom;
    }

    private static String extension(String nom) {
        int point = nom.lastIndexOf('.');
        return point < 0 ? "" : nom.substring(point + 1).toLowerCase(Locale.ROOT);
    }
}
