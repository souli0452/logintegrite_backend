package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Spring Data ajoute automatiquement un WHERE type_fiche =
 * 'PERSONNE_PHYSIQUE' grâce au @DiscriminatorValue.
 * Le soft delete (@SQLRestriction) est hérité du parent.
 */
@Repository
public interface PersonnePhysiqueRepository
    extends JpaRepository<PersonnePhysique, Long> {

    Page<PersonnePhysique> findByStatutFiche(
        StatutFiche statut, Pageable pageable);

    Optional<PersonnePhysique> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    Page<PersonnePhysique> findByCreateurId(
        String createurId, Pageable pageable);
        
        // Recherche spécifique PP 
    @Query("""
        SELECT p FROM PersonnePhysique p
        LEFT JOIN FETCH p.entite
        LEFT JOIN FETCH p.region
        WHERE (:statut IS NULL OR CAST(p.statutFiche AS string) = :statut)
          AND (:nom IS NULL OR 
               CAST(p.nom AS string) = :nom 
               OR CAST(p.prenoms AS string) = :nom)
          AND (:entiteId IS NULL OR p.entite.id = :entiteId)
          AND (:regionId IS NULL OR p.region.id = :regionId)
    """)
    Page<PersonnePhysique> rechercheAvancee(
        @Param("nom") String nom,
        @Param("entiteId") Long entiteId,
        @Param("regionId") Long regionId,
        @Param("statut") String statut,
        Pageable pageable
    );

}
