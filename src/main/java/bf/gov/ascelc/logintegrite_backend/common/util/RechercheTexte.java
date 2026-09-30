package bf.gov.ascelc.logintegrite_backend.common.util;

/** Preparation des termes de recherche saisis par l'utilisateur. */
public final class RechercheTexte {

    private RechercheTexte() { }

    /** Terme nettoye ; chaine vide si absent (les requetes testent "= ''" plutot que "is null", plus sur avec PostgreSQL). */
    public static String normaliser(String terme) {
        return terme == null ? "" : terme.trim();
    }

    /** Echappe les jokers LIKE (\\, % et _) pour que la saisie soit comprise comme du texte litteral (clause ESCAPE '\\'). */
    public static String echapperLike(String terme) {
        return normaliser(terme)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
