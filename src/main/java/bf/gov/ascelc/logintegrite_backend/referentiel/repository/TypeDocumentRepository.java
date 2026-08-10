// referentiel/repository/TypeDocumentRepository.java
package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TypeDocumentRepository extends JpaRepository<TypeDocument, UUID> {
}
