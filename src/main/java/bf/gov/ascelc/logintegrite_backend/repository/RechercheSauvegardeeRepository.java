package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.RechercheSauvegardee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface RechercheSauvegardeeRepository extends JpaRepository<RechercheSauvegardee, Long> {

    @Query(value = "SELECT * FROM recherche_sauvegardee WHERE utilisateur_id = :utilisateurId", nativeQuery = true)
    List<RechercheSauvegardee> findByUtilisateurId(@Param("utilisateurId") String utilisateurId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM recherche_sauvegardee WHERE id = :id AND utilisateur_id = :utilisateurId", nativeQuery = true)
    void deleteByIdAndUtilisateurId(@Param("id") Long id, @Param("utilisateurId") String utilisateurId);
}