package bf.gov.ascelc.logintegrite_backend.personne.repository;

import bf.gov.ascelc.logintegrite_backend.personne.entity.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AliasRepository extends JpaRepository<Alias, UUID> {
}
