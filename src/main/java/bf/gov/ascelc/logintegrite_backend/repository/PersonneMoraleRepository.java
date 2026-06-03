package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonneMoraleRepository
    extends JpaRepository<PersonneMorale, Long> {

    Page<PersonneMorale> findByStatutFiche(
        StatutFiche statut, Pageable pageable);

    Optional<PersonneMorale> findByIfu(String ifu);

    boolean existsByIfu(String ifu);

    @Query("""
        SELECT pm FROM PersonneMorale pm
        WHERE pm.statutFiche = 'ACTIVE'
          AND (:raisonSociale IS NULL OR
               LOWER(pm.raisonSociale)
               LIKE LOWER(CONCAT('%',:raisonSociale,'%')))
          AND (:entiteId IS NULL OR pm.entite.id = :entiteId)
          AND (:regionId IS NULL OR pm.region.id = :regionId)
          AND (:typeStructure IS NULL
               OR pm.typeStructure = :typeStructure)
    """)
    Page<PersonneMorale> rechercheAvancee(
        @Param("raisonSociale") String raisonSociale,
        @Param("entiteId") Long entiteId,
        @Param("regionId") Long regionId,
        @Param("typeStructure") String typeStructure,
        Pageable pageable
    );
}
