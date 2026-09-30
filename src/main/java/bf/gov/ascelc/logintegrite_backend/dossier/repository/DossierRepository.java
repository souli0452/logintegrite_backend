// dossier/repository/DossierRepository.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DossierRepository extends JpaRepository<Dossier, UUID> {
    long countByStatutDossier(StatutDossier statut);

    /**
     * Recherche par numero ou intitule (texte deja echappe pour LIKE) et filtre de statut optionnel.
     * "q = ''" et "statut = ''" signifient "pas de filtre" (evite les parametres nuls, mal types par PostgreSQL).
     */
    @Query("""
            select d from Dossier d
            where (:statut = '' or cast(d.statutDossier as string) = :statut)
              and (:q = ''
                   or lower(coalesce(d.numeroDossier, '')) like lower(concat('%', :q, '%')) escape '\\'
                   or lower(coalesce(d.intitule, '')) like lower(concat('%', :q, '%')) escape '\\')
            """)
    Page<Dossier> rechercher(@Param("q") String q, @Param("statut") String statut, Pageable pageable);
}
