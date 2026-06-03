// ── INTERFACE ──────────────────────────────────────────────────
package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonnePhysiqueService
    extends FicheMiseEnCauseService {

    PersonnePhysique creer(PersonnePhysique pp);
    PersonnePhysique modifier(Long id, PersonnePhysique pp);
    Page<PersonnePhysique> lister(StatutFiche statut,
                                   Pageable pageable);
    Page<PersonnePhysique> rechercherPP(
        String nom, Long entiteId,
        Long regionId, String statut,
        Pageable pageable);
}
