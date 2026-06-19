package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.RechercheSauvegardee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RechercheSauvegardeeRepository extends JpaRepository<RechercheSauvegardee, UUID> {

    List<RechercheSauvegardee> findByCreatedByIdOrderByCreatedAtDesc(String createdById);
}