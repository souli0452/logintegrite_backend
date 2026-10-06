package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import bf.gov.ascelc.logintegrite_backend.rapport.service.impl.PdfEcriture;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Fabrique le PDF officiel et l'Excel d'un etat trimestriel. */
public final class EtatTrimestrielDocuments {

    private static final DateTimeFormatter JJMMAAAA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private EtatTrimestrielDocuments() { }

    public static byte[] pdf(PeriodeTrimestre periode, LocalDate etabliLe, SyntheseTrimestre synthese,
                             List<LigneRegistre> lignes) {
        try (PDDocument document = new PDDocument();
             PdfEcriture pdf = new PdfEcriture(document, "Etat trimestriel " + periode.libelle())) {
            pdf.titre("ETAT TRIMESTRIEL DES PERSONNES FICHEES - " + periode.libelle());
            pdf.ligne("Autorite Superieure de Controle d'Etat et de Lutte contre la Corruption");
            pdf.ligne("Date d'arret : " + periode.fin().format(JJMMAAAA));
            pdf.ligne("Etabli le : " + etabliLe.format(JJMMAAAA));
            pdf.espace(10);

            pdf.ligneGrasse("Synthese du trimestre");
            pdf.ligne("Nombre de personnes fichees : " + lignes.size());
            if (synthese.premierEtat()) {
                pdf.ligne("Ceci est le premier etat : aucune comparaison avec un etat precedent.");
            } else {
                pdf.ligne("Nouvelles personnes fichees : " + synthese.nouveaux().size());
                for (String n : synthese.nouveaux()) pdf.ligne("  + " + n);
                pdf.ligne("Personnes sorties du registre : " + synthese.sortis().size());
                for (String n : synthese.sortis()) pdf.ligne("  - " + n);
            }
            pdf.ligne("Changements de statut judiciaire : " + synthese.changementsStatut());
            pdf.ligne("Dossiers clos : " + synthese.dossiersClos());
            pdf.espace(10);

            pdf.ligneGrasse("Liste des personnes fichees");
            pdf.ligneTableau(String.format("%-5s %-46s %-10s %s", "N", "NOM / DENOMINATION", "TYPE", "DOSSIERS"));
            pdf.ligneTableau("-".repeat(72));
            int numero = 1;
            for (LigneRegistre l : lignes) {
                pdf.ligneTableau(String.format("%-5d %-46s %-10s %d",
                        numero++, tronquer(l.nom(), 46), l.type(), l.dossiers()));
            }
            pdf.terminer();
            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            document.save(sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation du PDF de l'etat trimestriel", e);
        }
    }

    public static byte[] excel(PeriodeTrimestre periode, LocalDate etabliLe, SyntheseTrimestre synthese,
                               List<LigneRegistre> lignes) {
        try (SXSSFWorkbook classeur = new SXSSFWorkbook(200)) {
            Sheet resume = classeur.createSheet("Synthese");
            resume.setColumnWidth(0, 45 * 256);
            resume.setColumnWidth(1, 25 * 256);
            String[][] valeurs = {
                    {"Periode", periode.libelle()},
                    {"Date d'arret", periode.fin().format(JJMMAAAA)},
                    {"Etabli le", etabliLe.format(JJMMAAAA)},
                    {"Personnes fichees", String.valueOf(lignes.size())},
                    {"Nouvelles personnes fichees",
                            synthese.premierEtat() ? "premier etat" : String.valueOf(synthese.nouveaux().size())},
                    {"Personnes sorties du registre",
                            synthese.premierEtat() ? "premier etat" : String.valueOf(synthese.sortis().size())},
                    {"Changements de statut judiciaire", String.valueOf(synthese.changementsStatut())},
                    {"Dossiers clos", String.valueOf(synthese.dossiersClos())}};
            for (int i = 0; i < valeurs.length; i++) {
                Row r = resume.createRow(i);
                r.createCell(0).setCellValue(valeurs[i][0]);
                r.createCell(1).setCellValue(valeurs[i][1]);
            }

            Sheet feuille = classeur.createSheet("Personnes");
            feuille.setColumnWidth(0, 50 * 256);
            feuille.setColumnWidth(1, 14 * 256);
            feuille.setColumnWidth(2, 12 * 256);
            Row entete = feuille.createRow(0);
            entete.createCell(0).setCellValue("Nom / Denomination");
            entete.createCell(1).setCellValue("Type");
            entete.createCell(2).setCellValue("Dossiers");
            int ligne = 1;
            for (LigneRegistre l : lignes) {
                Row r = feuille.createRow(ligne++);
                r.createCell(0).setCellValue(l.nom() != null ? l.nom() : "-");
                r.createCell(1).setCellValue(l.type());
                r.createCell(2).setCellValue(l.dossiers());
            }
            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            classeur.write(sortie);
            classeur.dispose();
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation Excel de l'etat trimestriel", e);
        }
    }

    private static String tronquer(String s, int max) {
        if (s == null) return "-";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }
}
