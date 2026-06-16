package bf.gov.ascelc.logintegrite_backend.utils.constants;

public final class ApiURLs {

    private ApiURLs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Racines de l'API (Conforme à tes deux formats existants)
    public static final String API_ROOT = "/api";
    public static final String API_V1_ROOT = "/api/v1";

    // Fiches, Personnes Morales & Physiques
    public static final String FICHES = API_ROOT + "/fiches";
    public static final String FICHES_ID = "/{id}";
    public static final String FICHES_SOUMETTRE = "/{id}/soumettre";
    public static final String FICHES_VALIDER = "/{id}/valider";
    public static final String FICHES_REJETER = "/{id}/rejeter";
    public static final String FICHES_ARCHIVER = "/{id}/archiver";
    public static final String FICHES_STATUT_JUDICIAIRE = "/{id}/statut-judiciaire";
    public static final String FICHES_RECHERCHE = "/recherche";

    public static final String PERSONNES_MORALES = FICHES + "/personnes-morales";
    public static final String PERSONNES_MORALES_RECHERCHE = "/recherche";

    public static final String PERSONNES_PHYSIQUES = FICHES + "/personnes-physiques";
    public static final String PERSONNES_PHYSIQUES_RECHERCHE = "/recherche";

    // Audit
    public static final String AUDIT = API_ROOT + "/audit";
    public static final String AUDIT_MES_ACTIONS = "/mes-actions";

    // Notifications
    public static final String NOTIFICATIONS = API_ROOT + "/notifications";
    public static final String NOTIFICATIONS_COUNT = "/count-non-lues";
    public static final String NOTIFICATIONS_MARQUER_LUES = "/marquer-lues";

    // Rapports (Calé sur ton préfixe v1)
    public static final String RAPPORTS = API_V1_ROOT + "/rapports";
    public static final String RAPPORTS_PDF = "/pdf";
    public static final String RAPPORTS_EXCEL = "/excel";

    // Référentiels
    public static final String REFERENTIEL = API_ROOT + "/referentiel";
    public static final String REFERENTIEL_REGIONS = "/regions";
    public static final String REFERENTIEL_ENTITES = "/entites";
    public static final String REFERENTIEL_TYPES_INFRACTION = "/types-infraction";

    // Statistiques
    public static final String STATISTIQUES = API_ROOT + "/statistiques";

    //Sauvegardes
    public static final String SAUVEGARDES = API_ROOT + "/sauvegardes";
}