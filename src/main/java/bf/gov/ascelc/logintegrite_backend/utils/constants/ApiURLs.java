package bf.gov.ascelc.logintegrite_backend.utils.constants;

public final class ApiURLs {

    private ApiURLs() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String API_ROOT = "/api";
    public static final String API_V1_ROOT = "/api/v1";

    public static final String FICHES = API_V1_ROOT + "/fiches";
    public static final String FICHES_ID = "/{id}";
    public static final String FICHES_SOUMETTRE = "/{id}/soumettre";
    public static final String FICHES_VALIDER = "/{id}/valider";
    public static final String FICHES_REJETER = "/{id}/rejeter";
    public static final String FICHES_ARCHIVER = "/{id}/archiver";
    public static final String FICHES_STATUT_JUDICIAIRE = "/{id}/statut-judiciaire";

    public static final String FICHES_RECHERCHE = "/recherche";
    public static final String FICHES_MES_ACTIONS_RECENTES = "/mes-actions-recentes";

    // AJOUT : file d'attente de validation (EN_ATTENTE_VALIDATION, tous créateurs)
    public static final String FICHES_A_VALIDER = "/a-valider";

    // AJOUT : actions rapides du validateur (fiches qu'IL a validées/rejetées)
    public static final String FICHES_MES_ACTIONS_VALIDATEUR = "/mes-actions-validateur";

    public static final String PERSONNES_MORALES = API_V1_ROOT + "/personnes-morales";
    public static final String PERSONNES_MORALES_RECHERCHE = "/recherche";

    public static final String PERSONNES_PHYSIQUES = API_V1_ROOT + "/personnes-physiques";
    public static final String PERSONNES_PHYSIQUES_RECHERCHE = "/recherche";

    public static final String AUDIT = API_V1_ROOT + "/audit";
    public static final String AUDIT_MES_ACTIONS = "/mes-actions";

    public static final String NOTIFICATIONS = API_V1_ROOT + "/notifications";
    public static final String NOTIFICATIONS_COUNT = "/count-non-lues";
    public static final String NOTIFICATIONS_MARQUER_LUES = "/marquer-lues";

    public static final String RAPPORTS = API_V1_ROOT + "/rapports";
    public static final String RAPPORTS_PDF = "/pdf";
    public static final String RAPPORTS_EXCEL = "/excel";

    public static final String REFERENTIEL = API_V1_ROOT + "/referentiel";
    public static final String REFERENTIEL_REGIONS = "/regions";
    public static final String REFERENTIEL_ENTITES = "/entites";
    public static final String REFERENTIEL_TYPES_INFRACTION = "/types-infraction";
    public static final String REFERENTIEL_STATUTS_JUDICIAIRES = "/statuts-judiciaires";

    public static final String STATISTIQUES = API_V1_ROOT + "/statistiques";

    public static final String SAUVEGARDES = API_V1_ROOT + "/sauvegardes";
}