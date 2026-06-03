// NotificationRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository
    extends JpaRepository<Notification, Long> {

    Page<Notification> findByDestinataireIdOrderByDateEnvoiDesc(
        String destinataireId, Pageable pageable);

    long countByDestinataireIdAndLueFalse(String destinataireId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true " +
           "WHERE n.destinataireId = :destinataireId")
    void marquerToutesLues(String destinataireId);
}
