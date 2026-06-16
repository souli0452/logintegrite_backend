package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonnePhysiqueRepository extends JpaRepository<PersonnePhysique, UUID> {

    // 1. Assure-toi que cette méthode prend bien un String en paramètre
    Page<PersonnePhysique> findByStatutFiche(String statutFiche, Pageable pageable);

    boolean existsByMatricule(String matricule);

    // 2. Vérifie aussi ta requête personnalisée (JPQL / SQL) avec UUID
    @Query("SELECT p FROM PersonnePhysique p WHERE " +
            "(:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
            "(:entiteId IS NULL OR p.entite.id = :entiteId) AND " +
            "(:regionId IS NULL OR p.region.id = :regionId) AND " +
            "(:statut IS NULL OR p.statutFiche = :statut)")
    Page<PersonnePhysique> rechercheAvancee(
            @Param("nom") String nom,
            @Param("entiteId") UUID entiteId,
            @Param("regionId") UUID regionId,
            @Param("statut") String statut, // Reste une String
            Pageable pageable);
}