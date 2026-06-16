package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import java.io.IOException;
import java.util.List;

public interface RapportService {
    byte[] genererPDF(String titre, List<FicheMiseEnCause> fiches) throws IOException;
    byte[] genererExcel(String titre, List<FicheMiseEnCause> fiches) throws IOException;
    List<FicheMiseEnCause> getFichesActives();
}