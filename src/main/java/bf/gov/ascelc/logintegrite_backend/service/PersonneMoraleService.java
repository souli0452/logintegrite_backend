package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import java.util.UUID;

public interface PersonneMoraleService extends FicheMiseEnCauseService {
    PersonneMorale creer(PersonneMorale pm);
    PersonneMorale modifier(UUID id, PersonneMorale pm);
}