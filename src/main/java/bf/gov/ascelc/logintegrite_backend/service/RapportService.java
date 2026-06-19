package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheExportResponse;
import java.io.IOException;
import java.util.List;

public interface RapportService {
    // CORRECTION : Toutes les signatures utilisent désormais le DTO FicheExportResponse
    byte[] genererPDF(String titre, List<FicheExportResponse> fiches) throws IOException;
    byte[] genererExcel(String titre, List<FicheExportResponse> fiches) throws IOException;
    List<FicheExportResponse> getFichesActivesPourExport();
}