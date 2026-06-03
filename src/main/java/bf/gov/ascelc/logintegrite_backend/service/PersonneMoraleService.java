// ── INTERFACE ──────────────────────────────────────────────────
package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutFiche;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PersonneMoraleService
    extends FicheMiseEnCauseService {

    PersonneMorale creer(PersonneMorale pm);
    PersonneMorale modifier(Long id, PersonneMorale pm);
    Page<PersonneMorale> lister(StatutFiche statut,
                                 Pageable pageable);
    Page<PersonneMorale> rechercherPM(
        String raisonSociale, Long entiteId,
        Long regionId, String typeStructure,
        Pageable pageable);
}
