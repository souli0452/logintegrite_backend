// document/repository/DocumentImplicationRepository.java
package bf.gov.ascelc.logintegrite_backend.document.repository;

import bf.gov.ascelc.logintegrite_backend.document.entity.DocumentImplication;
import bf.gov.ascelc.logintegrite_backend.document.entity.DocumentImplicationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentImplicationRepository extends JpaRepository<DocumentImplication, DocumentImplicationId> {
    boolean existsByDocument_IdAndImplication_Id(UUID documentId, UUID implicationId);
    void deleteByDocument_IdAndImplication_Id(UUID documentId, UUID implicationId);
}
