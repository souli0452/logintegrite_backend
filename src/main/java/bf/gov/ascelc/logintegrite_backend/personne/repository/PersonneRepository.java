// personne/repository/PersonneRepository.java (complet)
package bf.gov.ascelc.logintegrite_backend.personne.repository;

import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PersonneRepository extends JpaRepository<Personne, UUID>, JpaSpecificationExecutor<Personne> {
    long countByTypePersonne(TypePersonne typePersonne);
}
