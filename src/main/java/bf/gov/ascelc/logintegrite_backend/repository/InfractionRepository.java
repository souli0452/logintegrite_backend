package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InfractionRepository extends JpaRepository<Infraction, UUID> {

    List<Infraction> findByNature(String nature);

    List<Infraction> findByFicheId(UUID ficheId);

    @Query("SELECT COALESCE(ti.libelle, i.nature), COUNT(i) FROM Infraction i " +
            "JOIN i.fiche f LEFT JOIN i.typeInfraction ti WHERE " +
            "(:regionId IS NULL OR f.region.id = :regionId) AND " +
            "(:entiteId IS NULL OR f.entite.id = :entiteId) AND " +
            "(CAST(:dateDebut AS timestamp) IS NULL OR i.createdAt >= :dateDebut) AND " +
            "(CAST(:dateFin AS timestamp) IS NULL OR i.createdAt <= :dateFin) " +
            "GROUP BY COALESCE(ti.libelle, i.nature) ORDER BY COUNT(i) DESC")
    List<Object[]> countGroupByTypeInfraction(
            @Param("regionId") UUID regionId,
            @Param("entiteId") UUID entiteId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);
}