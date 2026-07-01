package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
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
public interface PersonnePhysiqueRepository extends JpaRepository<PersonnePhysique, UUID> {

    // Alignement du chemin d'attribut sur "entite"
    @EntityGraph(attributePaths = {"entite", "region"})
    Optional<PersonnePhysique> findWithRelationsById(UUID id);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Override
    List<PersonnePhysique> findAll();

    @EntityGraph(attributePaths = {"entite", "region"})
    Page<PersonnePhysique> findByStatutFiche(String statutFiche, Pageable pageable);

    boolean existsByMatricule(String matricule);

    long countByStatutFiche(String statutFiche);

    @EntityGraph(attributePaths = {"entite", "region"})
    @Query("SELECT p FROM PersonnePhysique p WHERE " +
            "(:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%')) OR LOWER(p.prenoms) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
            "(:entiteId IS NULL OR p.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR p.region.id = :regionId) AND " +
            "(:statut IS NULL OR p.statutFiche = :statut)")
    Page<PersonnePhysique> rechercheAvancee(
            @Param("nom") String nom,
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut,
            Pageable pageable);
}