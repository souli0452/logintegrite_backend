package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class EtatTrimestrielDocumentsTest {

    private static final PeriodeTrimestre T3 = new PeriodeTrimestre(2026, 3);
    private static final LocalDate ETABLI = LocalDate.of(2026, 10, 6);

    private static String texte(byte[] pdf) throws Exception {
        try (PDDocument d = Loader.loadPDF(pdf)) { return new PDFTextStripper().getText(d); }
    }

    @Test
    void pdfContientPeriodeDatesSyntheseEtListe() throws Exception {
        var lignes = List.of(new LigneRegistre(UUID.randomUUID(), "Alice Test", "PHYSIQUE", 2));
        var synthese = new SyntheseTrimestre(false, List.of("Alice Test"), List.of(), 4, 1);
        String t = texte(EtatTrimestrielDocuments.pdf(T3, ETABLI, synthese, lignes));
        assertThat(t).contains("T3 2026").contains("30/09/2026").contains("06/10/2026")
                .contains("Alice Test").contains("Nouvelles personnes fichees : 1");
    }

    @Test
    void premierEtatLeDit() throws Exception {
        var synthese = new SyntheseTrimestre(true, List.of(), List.of(), 0, 0);
        assertThat(texte(EtatTrimestrielDocuments.pdf(T3, ETABLI, synthese, List.of()))).contains("premier etat");
    }

    @Test
    void registreVideProduitDesDocumentsValides() throws Exception {
        var synthese = new SyntheseTrimestre(true, List.of(), List.of(), 0, 0);
        assertThat(texte(EtatTrimestrielDocuments.pdf(T3, ETABLI, synthese, List.of())))
                .contains("Nombre de personnes fichees : 0");
        try (Workbook w = new XSSFWorkbook(new ByteArrayInputStream(
                EtatTrimestrielDocuments.excel(T3, ETABLI, synthese, List.of())))) {
            assertThat(w.getNumberOfSheets()).isEqualTo(2);
        }
    }

    @Test
    void excelListeLesPersonnes() throws Exception {
        var lignes = List.of(new LigneRegistre(UUID.randomUUID(), "Alice Test", "PHYSIQUE", 2));
        var synthese = new SyntheseTrimestre(true, List.of(), List.of(), 0, 0);
        try (Workbook w = new XSSFWorkbook(new ByteArrayInputStream(
                EtatTrimestrielDocuments.excel(T3, ETABLI, synthese, lignes)))) {
            assertThat(w.getSheet("Personnes").getRow(1).getCell(0).getStringCellValue()).isEqualTo("Alice Test");
        }
    }
}
