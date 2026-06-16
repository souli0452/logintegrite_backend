package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Sauvegarde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SauvegardeRepository
        extends JpaRepository<Sauvegarde, UUID> { // Changé de Long à UUID

    List<Sauvegarde> findAllByOrderByDateDebutDesc();
}