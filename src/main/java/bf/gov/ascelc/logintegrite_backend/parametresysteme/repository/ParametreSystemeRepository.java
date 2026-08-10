package bf.gov.ascelc.logintegrite_backend.parametresysteme.repository;

import bf.gov.ascelc.logintegrite_backend.parametresysteme.entity.ParametreSysteme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, UUID> {
    Optional<ParametreSysteme> findByCle(String cle);
    boolean existsByCle(String cle);
}
