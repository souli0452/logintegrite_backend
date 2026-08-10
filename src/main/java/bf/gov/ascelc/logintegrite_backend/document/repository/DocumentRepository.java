// document/repository/DocumentRepository.java
package bf.gov.ascelc.logintegrite_backend.document.repository;

import bf.gov.ascelc.logintegrite_backend.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByDossierId(UUID dossierId);
    List<Document> findByDossierIdIn(List<UUID> dossierIds);

    // Non tague (visible partout) OU tague specifiquement a cette implication
    @Query("""
        SELECT d FROM Document d
        WHERE d.dossier.id = :dossierId
        AND (
            NOT EXISTS (SELECT 1 FROM DocumentImplication di WHERE di.document = d)
            OR EXISTS (SELECT 1 FROM DocumentImplication di WHERE di.document = d AND di.implication.id = :implicationId)
        )
        """)
    List<Document> findVisiblesPourImplication(@Param("dossierId") UUID dossierId, @Param("implicationId") UUID implicationId);
}
