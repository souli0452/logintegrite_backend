package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.NotificationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.NotificationResponse;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationResponse create(NotificationRequest request);
    NotificationResponse getById(UUID id);
    List<NotificationResponse> getMyNotifications(String destinataireId);
    List<NotificationResponse> getMyUnreadNotifications(String destinataireId);

    NotificationResponse markAsRead(UUID id, String userId);
    void markAllAsRead(String destinataireId);
    void delete(UUID id, String userId);

    // AJOUT : déclenchement métier centralisé, appelé directement depuis les
    // contrôleurs de fiches, juste à côté des appels auditService.log() déjà
    // existants (soumission/validation/rejet).
    void notifierUtilisateur(String destinataireId, String type, String contenu, String ressourceId, String ressourceType);
    void notifierRole(String role, String type, String contenu, String ressourceId, String ressourceType);
}