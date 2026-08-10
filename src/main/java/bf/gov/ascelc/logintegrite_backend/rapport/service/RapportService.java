package bf.gov.ascelc.logintegrite_backend.rapport.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import java.util.UUID;

public interface RapportService {
    byte[] genererPdfDossier(UUID dossierId);
    byte[] genererExcelRecherchePersonnes(PersonneSearchCriteria criteria);
    byte[] genererPdfRegistreOfficiel();       // NOUVEAU
    byte[] genererExcelDossiers();             // NOUVEAU
}
