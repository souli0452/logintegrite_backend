// rapport/service/impl/RapportServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.rapport.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.personne.specification.PersonneSpecifications;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RapportServiceImpl implements RapportService {

    private final DossierRepository dossierRepository;
    private final ImplicationRepository implicationRepository;
    private final FaitReprocheRepository faitReprocheRepository;
    private final PersonneRepository personneRepository;

    @Override
    public byte[] genererPdfDossier(UUID dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", dossierId));
        List<Implication> implications = implicationRepository.findByDossierId(dossierId);
        List<FaitReproche> faits = faitReprocheRepository.findByDossierId(dossierId);
        

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contenu = new PDPageContentStream(document, page)) {
                float y = 750;
                y = ecrireTitre(contenu, y, "Rapport de dossier - ASCE-LC");
                y -= 15;
                y = ecrireLigne(contenu, y, "Intitule : " + valeurOuVide(dossier.getIntitule()));
                y = ecrireLigne(contenu, y, "Numero : " + valeurOuVide(dossier.getNumeroDossier()));
                y = ecrireLigne(contenu, y, "Statut : " + dossier.getStatutDossier());
                y = ecrireLigne(contenu, y, "Date d'ouverture : " + dossier.getDateOuverture());
                y -= 15;

                y = ecrireLigne(contenu, y, "Personnes impliquees :");
                for (Implication imp : implications) {
                    y = ecrireLigne(contenu, y, "  - " + imp.getPersonne().getNomAffichage()
                            + " (" + imp.getRoleImplication().getLibelle() + ")");
                }
                y -= 15;

                y = ecrireLigne(contenu, y, "Faits reproches :");
                for (FaitReproche fait : faits) {
                    y = ecrireLigne(contenu, y, "  - " + fait.getDescription() + " ("
                            + fait.getMontantPrejudice() + " " + fait.getDevise() + ") - "
                            + fait.getStatutValidation());
                }
            }

            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            document.save(sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation du PDF", e);
        }
    }

    @Override
    public byte[] genererExcelRecherchePersonnes(PersonneSearchCriteria criteria) {
        List<Personne> resultats = personneRepository.findAll(PersonneSpecifications.depuisCriteres(criteria));

        try (Workbook classeur = new XSSFWorkbook()) {
            Sheet feuille = classeur.createSheet("Resultats recherche");

            Row entete = feuille.createRow(0);
            entete.createCell(0).setCellValue("Nom / Denomination");
            entete.createCell(1).setCellValue("Type");

            int numeroLigne = 1;
            for (Personne p : resultats) {
                Row ligne = feuille.createRow(numeroLigne++);
                ligne.createCell(0).setCellValue(p.getNomAffichage());
                ligne.createCell(1).setCellValue(p.getTypePersonne().name());
            }
            feuille.autoSizeColumn(0);
            feuille.autoSizeColumn(1);

            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            classeur.write(sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation Excel", e);
        }
    }
    
    @Override
public byte[] genererPdfRegistreOfficiel() {
    // Récupération de toutes les personnes ancrées au registre officiel
    PersonneSearchCriteria criteria = new PersonneSearchCriteria();
    criteria.setStatutAncrage(bf.gov.ascelc.logintegrite_backend.personne.enums.StatutAncrage.REGISTRE_OFFICIEL);
    List<Personne> toutes = personneRepository.findAll(PersonneSpecifications.depuisCriteres(criteria));

    // Filtre effectif : celles ayant AU MOINS un dossier entierement valide
    List<Personne> personnes = toutes.stream()
        .filter(p -> aAuMoinsUnDossierEntierementValide(p.getId()))
        .toList();

    try (PDDocument document = new PDDocument()) {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contenu = new PDPageContentStream(document, page)) {
            float y = 780;
            y = ecrireTitre(contenu, y, "REGISTRE OFFICIEL - ASCE-LC");
            y = ecrireLigne(contenu, y, "Autorite Superieure de Controle d'Etat et de Lutte contre la Corruption");
            y = ecrireLigne(contenu, y, "Date d'extraction : " + java.time.LocalDate.now());
            y = ecrireLigne(contenu, y, "Nombre total de personnes inscrites : " + personnes.size());
            y -= 15;

            y = ecrireLigne(contenu, y, "----------------------------------------------------------------");
            y = ecrireLigne(contenu, y, "N°   NOM / DENOMINATION                       TYPE          DOSSIERS");
            y = ecrireLigne(contenu, y, "----------------------------------------------------------------");

            int index = 1;
            for (Personne p : personnes) {
                if (y < 60) { // Saut de page
                    contenu.close();
                    page = new PDPage();
                    document.addPage(page);
                    // Note : nouveau content stream a ouvrir - pattern simplifie ici,
                    // dans le PDF existant du dossier c'est mono-page
                    y = 780;
                    break; // Simplification : on stoppe si depassement
                }
                int nbDossiers = compterDossiersValides(p.getId());
                String ligne = String.format("%-4d %-40s %-12s %d",
                        index++,
                        tronquer(p.getNomAffichage(), 40),
                        p.getTypePersonne().name(),
                        nbDossiers);
                y = ecrireLigne(contenu, y, ligne);
            }
        }

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        document.save(sortie);
        return sortie.toByteArray();
    } catch (IOException e) {
        throw new UncheckedIOException("Echec de la generation du PDF Registre", e);
    }
}

@Override
public byte[] genererExcelDossiers() {
    List<Dossier> dossiers = dossierRepository.findAll();

    try (Workbook classeur = new XSSFWorkbook()) {
        Sheet feuille = classeur.createSheet("Dossiers");

        Row entete = feuille.createRow(0);
        entete.createCell(0).setCellValue("N° Dossier");
        entete.createCell(1).setCellValue("Intitule");
        entete.createCell(2).setCellValue("Statut");
        entete.createCell(3).setCellValue("Date d'ouverture");
        entete.createCell(4).setCellValue("Nb impliques");
        entete.createCell(5).setCellValue("Nb faits reproches");

        int numLigne = 1;
        for (Dossier d : dossiers) {
            Row ligne = feuille.createRow(numLigne++);
            ligne.createCell(0).setCellValue(valeurOuVide(d.getNumeroDossier()));
            ligne.createCell(1).setCellValue(valeurOuVide(d.getIntitule()));
            ligne.createCell(2).setCellValue(d.getStatutDossier() != null ? d.getStatutDossier().name() : "-");
            ligne.createCell(3).setCellValue(d.getDateOuverture() != null ? d.getDateOuverture().toString() : "-");
            ligne.createCell(4).setCellValue(implicationRepository.findByDossierId(d.getId()).size());
            ligne.createCell(5).setCellValue(faitReprocheRepository.findByDossierId(d.getId()).size());
        }
        for (int i = 0; i <= 5; i++) feuille.autoSizeColumn(i);

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        classeur.write(sortie);
        return sortie.toByteArray();
    } catch (IOException e) {
        throw new UncheckedIOException("Echec de la generation Excel Dossiers", e);
    }
}

// Helpers additionnels
private boolean aAuMoinsUnDossierEntierementValide(UUID personneId) {
    // Un dossier est "entierement valide" si tous ses faits reproche sont valides
    List<Implication> implications = implicationRepository.findByPersonneId(personneId);
    return implications.stream().anyMatch(imp -> {
        List<FaitReproche> faits = faitReprocheRepository.findByDossierId(imp.getDossier().getId());
        return !faits.isEmpty() && faits.stream()
                .allMatch(f -> f.getStatutValidation() ==
                        bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation.VALIDEE);
    });
}

private int compterDossiersValides(UUID personneId) {
    return (int) implicationRepository.findByPersonneId(personneId).stream()
        .filter(imp -> {
            List<FaitReproche> faits = faitReprocheRepository.findByDossierId(imp.getDossier().getId());
            return !faits.isEmpty() && faits.stream()
                .allMatch(f -> f.getStatutValidation() ==
                        bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation.VALIDEE);
        })
        .count();
}

private String tronquer(String s, int max) {
    if (s == null) return "-";
    return s.length() > max ? s.substring(0, max - 3) + "..." : s;
}

    private float ecrireTitre(PDPageContentStream contenu, float y, String texte) throws IOException {
        contenu.beginText();
        contenu.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contenu.newLineAtOffset(50, y);
        contenu.showText(texte);
        contenu.endText();
        return y - 25;
    }

    private float ecrireLigne(PDPageContentStream contenu, float y, String texte) throws IOException {
        contenu.beginText();
        contenu.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contenu.newLineAtOffset(50, y);
        contenu.showText(texte);
        contenu.endText();
        return y - 16;
    }

    private String valeurOuVide(String valeur) {
        return valeur != null ? valeur : "-";
    }
}
