package bf.gov.ascelc.logintegrite_backend.personne.repository;

import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonneMorale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonneMoraleRepository extends JpaRepository<PersonneMorale, UUID> {
}
