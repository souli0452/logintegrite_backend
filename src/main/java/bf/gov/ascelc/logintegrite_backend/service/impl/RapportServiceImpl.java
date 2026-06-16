package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
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

@Service
@RequiredArgsConstructor
public class RapportServiceImpl implements RapportService {

    private final FicheMiseEnCauseRepository ficheRepo;

    @Override
    @Transactional(readOnly = true)
    public byte[] genererPDF(String titre, List<FicheMiseEnCause> fiches) throws IOException {
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

        for (FicheMiseEnCause f : fiches) {
            table.addCell(f.getId() != null ? f.getId().toString() : "");

            if (f instanceof PersonnePhysique pp) {
                String nomComplet = (pp.getNom() != null ? pp.getNom() : "") + " " + (pp.getPrenoms() != null ? pp.getPrenoms() : "");
                table.addCell(nomComplet.trim().isEmpty() ? "Physique Anonyme" : nomComplet.trim());
            } else if (f instanceof PersonneMorale pm) {
                table.addCell(pm.getRaisonSociale() != null ? pm.getRaisonSociale() : "Morale Sans Nom");
            } else {
                table.addCell("Cible indéterminée");
            }

            table.addCell(f.getEntite() != null ? f.getEntite().getNom() : "-");
            table.addCell(f.getStatutJudiciaire() != null ? f.getStatutJudiciaire() : "-");
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] genererExcel(String titre, List<FicheMiseEnCause> fiches) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Log Intégrité");

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] cols = {"ID", "Type Cible", "Nom / Raison Sociale", "Prénoms / Sigle", "Identifiant Unique (Matricule/IFU)", "Entité", "Région", "Nature infraction", "Statut judiciaire", "Statut fiche"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (FicheMiseEnCause f : fiches) {
            Row row = sheet.createRow(rowNum++);

            // CORRECTION : Conversion de l'UUID en String pour la cellule Excel
            row.createCell(0).setCellValue(f.getId() != null ? f.getId().toString() : "");

            if (f instanceof PersonnePhysique pp) {
                row.createCell(1).setCellValue("PERSONNE PHYSIQUE");
                row.createCell(2).setCellValue(pp.getNom() != null ? pp.getNom() : "");
                row.createCell(3).setCellValue(pp.getPrenoms() != null ? pp.getPrenoms() : "");
                row.createCell(4).setCellValue(pp.getMatricule() != null ? pp.getMatricule() : "");
            } else if (f instanceof PersonneMorale pm) {
                row.createCell(1).setCellValue("PERSONNE MORALE");
                row.createCell(2).setCellValue(pm.getRaisonSociale() != null ? pm.getRaisonSociale() : "");
                row.createCell(3).setCellValue(pm.getSigle() != null ? pm.getSigle() : "");
                row.createCell(4).setCellValue(pm.getIfu() != null ? pm.getIfu() : "");
            } else {
                row.createCell(1).setCellValue("INCONNU");
                row.createCell(2).setCellValue("");
                row.createCell(3).setCellValue("");
                row.createCell(4).setCellValue("");
            }

            row.createCell(5).setCellValue(f.getEntite() != null ? f.getEntite().getNom() : "");
            row.createCell(6).setCellValue(f.getRegion() != null ? f.getRegion().getNom() : "");

            row.createCell(7).setCellValue(
                    f.getInfractions() != null && !f.getInfractions().isEmpty() && f.getInfractions().get(0).getNature() != null
                            ? f.getInfractions().get(0).getNature()
                            : ""
            );

            row.createCell(8).setCellValue(f.getStatutJudiciaire() != null ? f.getStatutJudiciaire() : "");
            row.createCell(9).setCellValue(f.getStatutFiche() != null ? f.getStatutFiche() : "");
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
    public List<FicheMiseEnCause> getFichesActives() {
        return ficheRepo.findAll().stream()
                .filter(f -> "ACTIVE".equals(f.getStatutFiche()))
                .toList();
    }
}