package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonneMoraleRepository extends JpaRepository<PersonneMorale, UUID> {

    // Alignement du champ sur "entite" pour correspondre à la structure de l'entité
    @EntityGraph(attributePaths = {"entite", "region"})
    Optional<PersonneMorale> findWithRelationsById(UUID id);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Override
    List<PersonneMorale> findAll();

    long countByStatutFiche(String statutFiche);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT m FROM PersonneMorale m WHERE " +
            "(:raisonSociale IS NULL OR LOWER(m.raisonSociale) LIKE LOWER(CONCAT('%', :raisonSociale, '%'))) AND " +
            "(:entiteId IS NULL OR m.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR m.region.id = :regionId) AND " +
            "(:statut IS NULL OR m.statutFiche = :statut) AND " +
            "(:typeStructure IS NULL OR m.typeStructure = :typeStructure)")
    Page<PersonneMorale> rechercheAvancee(
            @Param("raisonSociale") String raisonSociale,
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut,
            @Param("typeStructure") String typeStructure,
            Pageable pageable);
}