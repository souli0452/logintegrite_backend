package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonneMoraleRepository extends JpaRepository<PersonneMorale, UUID> {

    Page<PersonneMorale> findByStatutFiche(String statutFiche, Pageable pageable);

    @Query("SELECT pm FROM PersonneMorale pm WHERE " +
            "(:raisonSociale IS NULL OR LOWER(pm.raisonSociale) LIKE LOWER(CONCAT('%', :raisonSociale, '%'))) AND " +
            "(:entiteId IS NULL OR pm.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR pm.region.id = :regionId) AND " +
            "(:typeStructure IS NULL OR pm.typeStructure = :typeStructure) AND " +
            "(:statut IS NULL OR pm.statutFiche = :statut)") // Alignement parfait avec PersonnePhysique
    Page<PersonneMorale> rechercheAvancee(
            @Param("raisonSociale") String raisonSociale,
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("typeStructure") String typeStructure,
            @Param("statut") String statut, // Ajout du paramètre statut
            Pageable pageable);
}