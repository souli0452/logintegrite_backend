package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Utilisation du comptage SQL natif pour la pagination
    @Query(value = "SELECT * FROM notification WHERE destinataire_id = :destinataireId ORDER BY date_envoi DESC",
            countQuery = "SELECT count(*) FROM notification WHERE destinataire_id = :destinataireId",
            nativeQuery = true)
    Page<Notification> findByDestinataireIdOrderByDateEnvoiDesc(@Param("destinataireId") String destinataireId, Pageable pageable);

    @Query(value = "SELECT count(*) FROM notification WHERE destinataire_id = :destinataireId AND lue = false", nativeQuery = true)
    long countByDestinataireIdAndLueFalse(@Param("destinataireId") String destinataireId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE notification SET lue = true WHERE destinataire_id = :destinataireId", nativeQuery = true)
    void marquerToutesLues(@Param("destinataireId") String destinataireId);
}