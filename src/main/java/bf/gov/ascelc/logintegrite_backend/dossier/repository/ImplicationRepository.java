// dossier/repository/ImplicationRepository.java
package bf.gov.ascelc.logintegrite_backend.dossier.repository;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ImplicationRepository extends JpaRepository<Implication, UUID> {
    List<Implication> findByDossierId(UUID dossierId);
    List<Implication> findByPersonneId(UUID personneId);

    /** Implications d'un dossier avec personne et role charges en une seule requete (pas de N+1). */
    @Query("SELECT i FROM Implication i JOIN FETCH i.personne JOIN FETCH i.roleImplication WHERE i.dossier.id = :dossierId")
    List<Implication> findByDossierIdAvecPersonneEtRole(@Param("dossierId") UUID dossierId);

    /** [dossierId, nombre d'implications] pour tous les dossiers, en une requete. */
    @Query("SELECT i.dossier.id, COUNT(i) FROM Implication i GROUP BY i.dossier.id")
    List<Object[]> compterParDossier();

    /**
     * [personneId, nombre de dossiers entierement valides] : un dossier est entierement valide quand
     * il a au moins un fait reproche et qu'aucun n'est autre que VALIDEE. Une requete pour toutes les personnes.
     */
    @Query("""
            SELECT i.personne.id, COUNT(DISTINCT i.dossier.id) FROM Implication i
            WHERE EXISTS (SELECT 1 FROM FaitReproche f WHERE f.dossier = i.dossier)
              AND NOT EXISTS (SELECT 1 FROM FaitReproche f2 WHERE f2.dossier = i.dossier AND f2.statutValidation <> :valide)
            GROUP BY i.personne.id
            """)
    List<Object[]> compterDossiersEntierementValidesParPersonne(@Param("valide") StatutValidation valide);
}
