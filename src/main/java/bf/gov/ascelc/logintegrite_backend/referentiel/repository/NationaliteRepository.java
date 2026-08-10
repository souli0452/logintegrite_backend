package bf.gov.ascelc.logintegrite_backend.referentiel.repository;

import bf.gov.ascelc.logintegrite_backend.referentiel.entity.Nationalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NationaliteRepository extends JpaRepository<Nationalite, UUID> {

    /** Toutes les nationalités actives, triées alphabétiquement (Burkinabè en tête via order by). */
    List<Nationalite> findByActifTrueOrderByLibelleAsc();

    /** Recherche par libellé exact — utilisé pour la migration douce (matching texte → ID). */
    Optional<Nationalite> findByLibelleIgnoreCase(String libelle);

    boolean existsByLibelleIgnoreCase(String libelle);
}
