package bf.gov.ascelc.logintegrite_backend.utils.constants;

public final class ApiURLs {

    private ApiURLs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Racines de l'API
    public static final String API_ROOT = "/api";
    public static final String API_V1_ROOT = "/api/v1";

    // Fiches (Gérées par FicheController)
    public static final String FICHES = API_V1_ROOT + "/fiches"; // /api/v1/fiches
    public static final String FICHES_ID = "/{id}";
    public static final String FICHES_SOUMETTRE = "/{id}/soumettre";
    public static final String FICHES_VALIDER = "/{id}/valider";
    public static final String FICHES_REJETER = "/{id}/rejeter";
    public static final String FICHES_ARCHIVER = "/{id}/archiver";
    public static final String FICHES_STATUT_JUDICIAIRE = "/{id}/statut-judiciaire";
    public static final String FICHES_RECHERCHE = "/recherche";

    // Personnes Morales & Physiques
    public static final String PERSONNES_MORALES = API_V1_ROOT + "/personnes-morales"; // /api/v1/personnes-morales
    public static final String PERSONNES_MORALES_RECHERCHE = "/recherche";

    public static final String PERSONNES_PHYSIQUES = API_V1_ROOT + "/personnes-physiques"; // /api/v1/personnes-physiques
    public static final String PERSONNES_PHYSIQUES_RECHERCHE = "/recherche";

    // Audit
    public static final String AUDIT = API_V1_ROOT + "/audit";
    public static final String AUDIT_MES_ACTIONS = "/mes-actions";

    // Notifications
    public static final String NOTIFICATIONS = API_V1_ROOT + "/notifications";
    public static final String NOTIFICATIONS_COUNT = "/count-non-lues";
    public static final String NOTIFICATIONS_MARQUER_LUES = "/marquer-lues";

    // Rapports
    public static final String RAPPORTS = API_V1_ROOT + "/rapports";
    public static final String RAPPORTS_PDF = "/pdf";
    public static final String RAPPORTS_EXCEL = "/excel";

    // Référentiels
    public static final String REFERENTIEL = API_V1_ROOT + "/referentiel";
    public static final String REFERENTIEL_REGIONS = "/regions";
    public static final String REFERENTIEL_ENTITES = "/entites";
    public static final String REFERENTIEL_TYPES_INFRACTION = "/types-infraction";
    public static final String REFERENTIEL_STATUTS_JUDICIAIRES = "/statuts-judiciaires"; // AJOUT

    // Statistiques
    public static final String STATISTIQUES = API_V1_ROOT + "/statistiques";

    // Sauvegardes
    public static final String SAUVEGARDES = API_V1_ROOT + "/sauvegardes";
}