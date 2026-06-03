// RechercheSauvegardeeRepository.java
package bf.gov.ascelc.logintegrite_backend.repository;

import bf.gov.ascelc.logintegrite_backend.entity.RechercheSauvegardee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RechercheSauvegardeeRepository
    extends JpaRepository<RechercheSauvegardee, Long> {

    List<RechercheSauvegardee> findByUtilisateurId(
        String utilisateurId);

    void deleteByIdAndUtilisateurId(Long id, String utilisateurId);
}
