package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.CategorieInfraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategorieInfractionRepository extends JpaRepository<CategorieInfraction, UUID> {
}
