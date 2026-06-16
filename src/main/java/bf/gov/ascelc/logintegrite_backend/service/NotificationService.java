package bf.gov.ascelc.logintegrite_backend.service;

public interface NotificationService {
    void envoyer(String destinataireId, String type, String contenu);
    void notifierValidateur(String validateurId, String nomFiche);
    void notifierAgent(String agentId, String nomFiche, boolean validee, String motif);
    long countNonLues(String utilisateurId);
    void marquerToutesLues(String utilisateurId);
}