package bf.gov.ascelc.logintegrite_backend.personne.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** Normalisation et controle du NIP : 17 caracteres, chiffres et lettres majuscules. */
public final class NipUtil {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9]{17}$");

    private NipUtil() { }

    /** Retire les espaces et passe en majuscules ; un NIP vide devient null (champ non renseigne). */
    public static String normaliser(String brut) {
        if (brut == null) return null;
        String net = brut.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
        return net.isEmpty() ? null : net;
    }

    /** Vrai si le NIP (deja normalise) respecte le format attendu. */
    public static boolean estValide(String nipNormalise) {
        return nipNormalise != null && FORMAT.matcher(nipNormalise).matches();
    }
}
