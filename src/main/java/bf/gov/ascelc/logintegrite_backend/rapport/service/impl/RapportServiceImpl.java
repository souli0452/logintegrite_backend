// rapport/service/impl/RapportServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.rapport.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.enums.StatutAncrage;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.personne.specification.PersonneSpecifications;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Generation des rapports PDF et Excel.
 * <ul>
 *   <li>Les PDF s'etendent sur autant de pages que necessaire (jamais tronques).</li>
 *   <li>Les comptages se font en requetes groupees : pas de requete par ligne.</li>
 *   <li>Les listes sont lues par pages et les exports Excel ecrits en flux ; un plafond protege le serveur.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RapportServiceImpl implements RapportService {

    /** Au-dela, l'export est refuse : l'utilisateur doit affiner sa recherche. */
    static final int MAX_LIGNES_EXPORT = 50_000;
    private static final int TAILLE_PAGE = 500;

    private final DossierRepository dossierRepository;
    private final ImplicationRepository implicationRepository;
    private final FaitReprocheRepository faitReprocheRepository;
    private final PersonneRepository personneRepository;

    // -------------------------------------------------------------------------
    // PDF d'un dossier
    // -------------------------------------------------------------------------
    @Override
    public byte[] genererPdfDossier(UUID dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", dossierId));
        List<Implication> implications = implicationRepository.findByDossierIdAvecPersonneEtRole(dossierId);
        List<FaitReproche> faits = faitReprocheRepository.findByDossierId(dossierId);

        try (PDDocument document = new PDDocument();
             PdfEcriture pdf = new PdfEcriture(document, "Rapport de dossier " + valeurOuVide(dossier.getNumeroDossier()))) {
            pdf.titre("Rapport de dossier - ASCE-LC");
            pdf.espace(10);
            pdf.ligne("Intitule : " + valeurOuVide(dossier.getIntitule()));
            pdf.ligne("Numero : " + valeurOuVide(dossier.getNumeroDossier()));
            pdf.ligne("Statut : " + dossier.getStatutDossier());
            pdf.ligne("Date d'ouverture : " + dossier.getDateOuverture());
            pdf.espace(12);

            pdf.ligneGrasse("Personnes impliquees (" + implications.size() + ") :");
            for (Implication imp : implications) {
                pdf.ligne("  - " + imp.getPersonne().getNomAffichage() + " (" + imp.getRoleImplication().getLibelle() + ")");
            }
            pdf.espace(12);

            pdf.ligneGrasse("Faits reproches (" + faits.size() + ") :");
            for (FaitReproche fait : faits) {
                pdf.ligne("  - " + fait.getDescription() + " (" + fait.getMontantPrejudice() + " " + fait.getDevise()
                        + ") - " + fait.getStatutValidation());
            }
            pdf.terminer();
            return enOctets(document);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation du PDF", e);
        }
    }

    // -------------------------------------------------------------------------
    // PDF du registre officiel
    // -------------------------------------------------------------------------
    @Override
    public byte[] genererPdfRegistreOfficiel() {
        PersonneSearchCriteria criteres = new PersonneSearchCriteria();
        criteres.setStatutAncrage(StatutAncrage.REGISTRE_OFFICIEL);

        // Nombre de dossiers entierement valides par personne, pour toutes les personnes, en une requete.
        Map<UUID, Long> dossiersValides = compterParCle(
                implicationRepository.compterDossiersEntierementValidesParPersonne(StatutValidation.VALIDEE));

        List<LigneRegistre> lignes = new ArrayList<>();
        lirePersonnes(criteres, p -> {
            Long nb = dossiersValides.get(p.getId());
            if (nb != null && nb > 0) {
                lignes.add(new LigneRegistre(p.getNomAffichage(), p.getTypePersonne().name(), nb));
            }
        });

        try (PDDocument document = new PDDocument();
             PdfEcriture pdf = new PdfEcriture(document, "Registre officiel ASCE-LC")) {
            pdf.titre("REGISTRE OFFICIEL - ASCE-LC");
            pdf.ligne("Autorite Superieure de Controle d'Etat et de Lutte contre la Corruption");
            pdf.ligne("Date d'extraction : " + LocalDate.now());
            pdf.ligne("Nombre total de personnes inscrites : " + lignes.size());
            pdf.espace(10);
            pdf.ligneTableau(String.format("%-5s %-46s %-10s %s", "N", "NOM / DENOMINATION", "TYPE", "DOSSIERS"));
            pdf.ligneTableau("-".repeat(72));

            int numero = 1;
            for (LigneRegistre l : lignes) {
                pdf.ligneTableau(String.format("%-5d %-46s %-10s %d",
                        numero++, tronquer(l.nom(), 46), l.type(), l.dossiers()));
            }
            pdf.terminer();
            return enOctets(document);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation du PDF Registre", e);
        }
    }

    // -------------------------------------------------------------------------
    // Excel : recherche de personnes
    // -------------------------------------------------------------------------
    @Override
    public byte[] genererExcelRecherchePersonnes(PersonneSearchCriteria criteria) {
        try (SXSSFWorkbook classeur = new SXSSFWorkbook(200)) {
            Sheet feuille = classeur.createSheet("Resultats recherche");
            feuille.setColumnWidth(0, 50 * 256);
            feuille.setColumnWidth(1, 14 * 256);
            Row entete = feuille.createRow(0);
            entete.createCell(0).setCellValue("Nom / Denomination");
            entete.createCell(1).setCellValue("Type");

            int[] ligne = {1};
            lirePersonnes(criteria, p -> {
                Row r = feuille.createRow(ligne[0]++);
                r.createCell(0).setCellValue(valeurOuVide(p.getNomAffichage()));
                r.createCell(1).setCellValue(p.getTypePersonne().name());
            });
            return enOctets(classeur);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation Excel", e);
        }
    }

    // -------------------------------------------------------------------------
    // Excel : dossiers
    // -------------------------------------------------------------------------
    @Override
    public byte[] genererExcelDossiers() {
        Map<UUID, Long> nbImpliques = compterParCle(implicationRepository.compterParDossier());
        Map<UUID, Long> nbFaits = compterParCle(faitReprocheRepository.compterParDossier());

        try (SXSSFWorkbook classeur = new SXSSFWorkbook(200)) {
            Sheet feuille = classeur.createSheet("Dossiers");
            int[] largeurs = {18, 60, 12, 16, 14, 18};
            for (int i = 0; i < largeurs.length; i++) feuille.setColumnWidth(i, largeurs[i] * 256);
            Row entete = feuille.createRow(0);
            String[] titres = {"N° Dossier", "Intitule", "Statut", "Date d'ouverture", "Nb impliques", "Nb faits reproches"};
            for (int i = 0; i < titres.length; i++) entete.createCell(i).setCellValue(titres[i]);

            int numLigne = 1;
            int page = 0;
            Page<Dossier> courante;
            do {
                courante = dossierRepository.findAll(PageRequest.of(page++, TAILLE_PAGE,
                        Sort.by("dateOuverture").descending().and(Sort.by("id"))));
                if (courante.getTotalElements() > MAX_LIGNES_EXPORT) throw exportTropVolumineux(courante.getTotalElements());
                for (Dossier d : courante) {
                    Row ligne = feuille.createRow(numLigne++);
                    ligne.createCell(0).setCellValue(valeurOuVide(d.getNumeroDossier()));
                    ligne.createCell(1).setCellValue(valeurOuVide(d.getIntitule()));
                    ligne.createCell(2).setCellValue(d.getStatutDossier() != null ? d.getStatutDossier().name() : "-");
                    ligne.createCell(3).setCellValue(d.getDateOuverture() != null ? d.getDateOuverture().toString() : "-");
                    ligne.createCell(4).setCellValue(nbImpliques.getOrDefault(d.getId(), 0L));
                    ligne.createCell(5).setCellValue(nbFaits.getOrDefault(d.getId(), 0L));
                }
            } while (courante.hasNext());
            return enOctets(classeur);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec de la generation Excel Dossiers", e);
        }
    }

    // -------------------------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------------------------
    private record LigneRegistre(String nom, String type, long dossiers) { }

    /** Parcourt les personnes correspondant aux criteres, par pages, sans jamais tout charger en memoire. */
    private void lirePersonnes(PersonneSearchCriteria criteres, java.util.function.Consumer<Personne> action) {
        int page = 0;
        Page<Personne> courante;
        do {
            courante = personneRepository.findAll(PersonneSpecifications.depuisCriteres(criteres),
                    PageRequest.of(page++, TAILLE_PAGE, Sort.by("nomAffichage").and(Sort.by("id"))));
            if (courante.getTotalElements() > MAX_LIGNES_EXPORT) throw exportTropVolumineux(courante.getTotalElements());
            courante.forEach(action);
        } while (courante.hasNext());
    }

    private static Map<UUID, Long> compterParCle(List<Object[]> lignes) {
        Map<UUID, Long> resultat = new HashMap<>();
        for (Object[] l : lignes) resultat.put((UUID) l[0], ((Number) l[1]).longValue());
        return resultat;
    }

    private static IllegalArgumentException exportTropVolumineux(long total) {
        return new IllegalArgumentException("Export limite a " + MAX_LIGNES_EXPORT + " lignes (" + total
                + " correspondent) : affinez votre recherche.");
    }

    private static byte[] enOctets(PDDocument document) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        document.save(sortie);
        return sortie.toByteArray();
    }

    private static byte[] enOctets(SXSSFWorkbook classeur) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        classeur.write(sortie);
        classeur.dispose(); // supprime les fichiers temporaires du flux
        return sortie.toByteArray();
    }

    private static String tronquer(String s, int max) {
        if (s == null) return "-";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    private static String valeurOuVide(String valeur) {
        return valeur != null ? valeur : "-";
    }
}
