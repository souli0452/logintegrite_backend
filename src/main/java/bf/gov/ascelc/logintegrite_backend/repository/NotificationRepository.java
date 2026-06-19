package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Récupérer toutes les notifications d'un utilisateur triées par la plus récente
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(String destinataireId);

    // Utile pour afficher le badge de notifications non lues sur l'IHM
    List<Notification> findByDestinataireIdAndLueFalseOrderByCreatedAtDesc(String destinataireId);
}