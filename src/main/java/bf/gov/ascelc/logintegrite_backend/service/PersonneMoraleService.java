package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import java.util.List;
import java.util.UUID;

public interface PersonneMoraleService extends FicheMiseEnCauseService {
    PersonneMorale creer(PersonneMorale pm);
    PersonneMorale modifier(UUID id, PersonneMorale pm);
    List<PersonneMorale> listerTout();
    PersonneMorale consulter(UUID id);
}