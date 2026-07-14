package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.response.FicheExportResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.service.RapportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {

    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    @Transactional(readOnly = true)
    public byte[] genererPDF(String titre, List<FicheExportResponse> fiches) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf);

        Paragraph title = new Paragraph(titre)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        doc.add(title);

        doc.add(new Paragraph(
                "ASCE-LC — Log Intégrité — Généré le : "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(" "));

        Table table = new Table(new float[]{1.5f, 4.5f, 2f, 2f});
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

        String[] headers = {"ID", "Identité / Raison Sociale", "Entité", "Statut judiciaire"};
        for (String h : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(h).setBold())
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(28, 107, 53))
                    .setFontColor(ColorConstants.WHITE);
            table.addHeaderCell(cell);
        }

        for (FicheExportResponse f : fiches) {
            table.addCell(f.getId() != null ? f.getId().toString() : "");
            table.addCell(f.getCibleNom() != null ? f.getCibleNom() : "");
            table.addCell(f.getStructureAssociee() != null ? f.getStructureAssociee() : "-");
            table.addCell(f.getStatutJudiciaire() != null ? f.getStatutJudiciaire() : "-");
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] genererExcel(String titre, List<FicheExportResponse> fiches) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log Intégrité");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] cols = {"ID", "Type Cible", "Nom / Raison Sociale", "Identifiant Unique (Matricule/IFU)", "Entité", "Région", "Statut judiciaire", "Statut fiche"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;

        for (FicheExportResponse f : fiches) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(f.getId() != null ? f.getId().toString() : "");
            row.createCell(1).setCellValue(f.getTypeFiche() != null ? f.getTypeFiche() : "");
            row.createCell(2).setCellValue(f.getCibleNom() != null ? f.getCibleNom() : "");
            row.createCell(3).setCellValue(f.getIdentifiantUnique() != null ? f.getIdentifiantUnique() : "");
            row.createCell(4).setCellValue(f.getStructureAssociee() != null ? f.getStructureAssociee() : "");
            row.createCell(5).setCellValue(f.getRegion() != null ? f.getRegion() : "");
            row.createCell(6).setCellValue(f.getStatutJudiciaire() != null ? f.getStatutJudiciaire() : "");
            row.createCell(7).setCellValue(f.getStatutFiche() != null ? f.getStatutFiche() : "");
        }

        for (int i = 0; i < cols.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FicheExportResponse> getFichesActivesPourExport() {

        return ficheRepo.findByStatutFiche("ACTIVE").stream() 
                .filter(f -> "ACTIVE".equals(f.getStatutFiche()))
                .map(fiche -> {
                    String cibleNom = "Inconnu";
                    String identifiant = "N/A";
                    String type = "AUTRE";

                    if (fiche instanceof PersonnePhysique pp) {
                        type = "Personne Physique"; 
                        String nomComplet = (pp.getNom() != null ? pp.getNom() : "") + " " + (pp.getPrenoms() != null ? pp.getPrenoms() : "");
                        cibleNom = nomComplet.trim().isEmpty() ? "Physique Anonyme" : nomComplet.trim();
                        identifiant = pp.getMatricule() != null ? pp.getMatricule() : "N/A";
                    } else if (fiche instanceof PersonneMorale pm) {
                        type = "Personne Morale";
                        cibleNom = pm.getRaisonSociale() != null ? pm.getRaisonSociale() : "Morale Sans Nom";
                        identifiant = pm.getIfu() != null ? pm.getIfu() : "N/A";
                    }

                    return FicheExportResponse.builder()
                            .id(fiche.getId())
                            .typeFiche(type)
                            .cibleNom(cibleNom)
                            .identifiantUnique(identifiant)
                            .structureAssociee(fiche.getEntite() != null ? fiche.getEntite().getNom() : "N/A")
                            .region(fiche.getRegion() != null ? fiche.getRegion().getNom() : "N/A")
                            .statutFiche(fiche.getStatutFiche())
                            .statutJudiciaire(fiche.getStatutJudiciaire())
                            .nbInfractions(fiche.getInfractions() != null ? fiche.getInfractions().size() : 0)
                            .createdAt(fiche.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
