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

    // AJOUT du paramètre userId : avant, n'importe quel utilisateur authentifié
    // pouvait marquer comme lue ou supprimer la notification de quelqu'un d'autre (IDOR)
    NotificationResponse markAsRead(UUID id, String userId);
    void markAllAsRead(String destinataireId);
    void delete(UUID id, String userId);
}
