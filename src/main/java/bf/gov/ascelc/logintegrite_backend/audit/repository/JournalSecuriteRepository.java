package bf.gov.ascelc.logintegrite_backend.audit.repository;

import bf.gov.ascelc.logintegrite_backend.audit.entity.JournalSecurite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface JournalSecuriteRepository extends JpaRepository<JournalSecurite, UUID> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "utilisateur")
    Page<JournalSecurite> findAllByOrderByDateEvenementDesc(Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "utilisateur")
    Page<JournalSecurite> findByTypeEvenementOrderByDateEvenementDesc(String type, Pageable pageable);

    /** Garde-fou contre l'inondation du journal par un poste (rafale de faux evenements). */
    long countByUtilisateurIdAndDateEvenementAfter(UUID utilisateurId, Instant depuis);

    long countByUtilisateurIdAndTypeEvenementAndDateEvenementAfter(UUID utilisateurId, String type, Instant depuis);
}
