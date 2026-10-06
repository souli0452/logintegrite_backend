package bf.gov.ascelc.logintegrite_backend.rapport.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import java.util.List;
import java.util.UUID;

public interface RapportService {
    byte[] genererPdfDossier(UUID dossierId);
    byte[] genererExcelRecherchePersonnes(PersonneSearchCriteria criteria);
    byte[] genererPdfRegistreOfficiel();       // NOUVEAU
    byte[] genererExcelDossiers();             // NOUVEAU

    /** Personnes du registre officiel (au moins un dossier entierement valide), avec leur nombre de dossiers valides. */
    List<LigneRegistre> lireRegistre();

    record LigneRegistre(UUID personneId, String nom, String type, long dossiers) { }
}
