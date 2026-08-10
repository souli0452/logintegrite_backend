// referentiel/repository/ZoneGeographiqueRepository.java
package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.ZoneGeographique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ZoneGeographiqueRepository extends JpaRepository<ZoneGeographique, UUID> {
}
