package bf.gov.ascelc.logintegrite_backend.rapport.service.impl;

import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePersonne;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.RoleImplication;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RapportServiceImplTest {

    private final DossierRepository dossiers = mock(DossierRepository.class);
    private final ImplicationRepository implications = mock(ImplicationRepository.class);
    private final FaitReprocheRepository faits = mock(FaitReprocheRepository.class);
    private final PersonneRepository personnes = mock(PersonneRepository.class);
    private final RapportServiceImpl service = new RapportServiceImpl(dossiers, implications, faits, personnes);

    private static Personne personne(UUID id, String nom) {
        Personne p = mock(Personne.class);
        when(p.getId()).thenReturn(id);
        when(p.getNomAffichage()).thenReturn(nom);
        when(p.getTypePersonne()).thenReturn(TypePersonne.PHYSIQUE);
        return p;
    }

    private static String texte(byte[] pdf) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private static int pages(byte[] pdf) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdf)) {
            return doc.getNumberOfPages();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void registreOfficielCouvreToutesLesPersonnesSurPlusieursPagesSansRequeteParLigne() throws Exception {
        List<Personne> toutes = new ArrayList<>();
        List<Object[]> valides = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            UUID id = UUID.randomUUID();
            toutes.add(personne(id, "Personne numero " + i));
            valides.add(new Object[]{id, 2L});
        }
        // Une personne du registre sans dossier entierement valide ne doit PAS apparaitre.
        toutes.add(personne(UUID.randomUUID(), "Personne exclue"));
        when(implications.compterDossiersEntierementValidesParPersonne(StatutValidation.VALIDEE)).thenReturn(valides);
        when(personnes.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(toutes, PageRequest.of(0, 500), toutes.size()));

        byte[] pdf = service.genererPdfRegistreOfficiel();

        String contenu = texte(pdf);
        int nbPages = pages(pdf);
        assertThat(nbPages).as("300 lignes ne tiennent pas sur une page").isGreaterThan(3);
        assertThat(contenu).contains("Nombre total de personnes inscrites : 300");
        assertThat(contenu).contains("Personne numero 1 ").contains("Personne numero 150 ").contains("Personne numero 300 ");
        assertThat(contenu).doesNotContain("Personne exclue");
        assertThat(contenu).contains("Page 1 / " + nbPages).contains("Page " + nbPages + " / " + nbPages);
        // Plus de requete par personne : uniquement le comptage groupe.
        verify(implications, never()).findByPersonneId(any());
        verify(implications, times(1)).compterDossiersEntierementValidesParPersonne(StatutValidation.VALIDEE);
    }

    @Test
    void pdfDuDossierNeTronquePasEtSupporteLesCaracteresExotiques() throws Exception {
        UUID id = UUID.randomUUID();
        Dossier dossier = mock(Dossier.class);
        when(dossier.getIntitule()).thenReturn("Marche ɛɔ ☃ fictif");
        when(dossier.getDateOuverture()).thenReturn(LocalDate.of(2026, 1, 1));
        when(dossiers.findById(id)).thenReturn(Optional.of(dossier));

        List<Implication> imps = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            Implication imp = mock(Implication.class);
            RoleImplication role = mock(RoleImplication.class);
            when(role.getLibelle()).thenReturn("Complice");
            Personne impliquee = personne(UUID.randomUUID(), "Impliquee " + i); // hors du when() : pas de stubbing imbrique
            when(imp.getPersonne()).thenReturn(impliquee);
            when(imp.getRoleImplication()).thenReturn(role);
            imps.add(imp);
        }
        FaitReproche fait = mock(FaitReproche.class);
        when(fait.getDescription()).thenReturn("Description tres longue ".repeat(200));
        when(fait.getMontantPrejudice()).thenReturn(BigDecimal.TEN);
        when(fait.getDevise()).thenReturn("XOF");
        when(implications.findByDossierIdAvecPersonneEtRole(id)).thenReturn(imps);
        when(faits.findByDossierId(id)).thenReturn(List.of(fait));

        byte[] pdf = service.genererPdfDossier(id);

        assertThat(pages(pdf)).isGreaterThan(1);
        String contenu = texte(pdf);
        assertThat(contenu).contains("Impliquee 0").contains("Impliquee 59").contains("Personnes impliquees (60)");
        assertThat(contenu).contains("Marche ??");
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportExcelRefuseAuDelaDuPlafond() {
        Page<Personne> enorme = new PageImpl<>(List.of(), PageRequest.of(0, 500), RapportServiceImpl.MAX_LIGNES_EXPORT + 1L);
        when(personnes.findAll(any(Specification.class), any(Pageable.class))).thenReturn(enorme);

        assertThatThrownBy(() -> service.genererExcelRecherchePersonnes(new PersonneSearchCriteria()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("affinez");
    }

    @Test
    void excelDesDossiersUtiliseDesComptagesGroupesEtNonUneRequeteParDossier() throws Exception {
        UUID d1 = UUID.randomUUID();
        UUID d2 = UUID.randomUUID();
        Dossier a = mock(Dossier.class);
        when(a.getId()).thenReturn(d1);
        when(a.getIntitule()).thenReturn("A");
        Dossier b = mock(Dossier.class);
        when(b.getId()).thenReturn(d2);
        when(b.getIntitule()).thenReturn("B");
        when(dossiers.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(a, b), PageRequest.of(0, 500), 2));
        when(implications.compterParDossier()).thenReturn(List.<Object[]>of(new Object[]{d1, 3L}));
        when(faits.compterParDossier()).thenReturn(List.<Object[]>of(new Object[]{d1, 5L}, new Object[]{d2, 1L}));

        byte[] xlsx = service.genererExcelDossiers();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            var feuille = wb.getSheet("Dossiers");
            assertThat(feuille.getLastRowNum()).isEqualTo(2);
            assertThat(feuille.getRow(1).getCell(4).getNumericCellValue()).isEqualTo(3);
            assertThat(feuille.getRow(1).getCell(5).getNumericCellValue()).isEqualTo(5);
            assertThat(feuille.getRow(2).getCell(4).getNumericCellValue()).isEqualTo(0); // aucun implique
        }
        verify(implications, never()).findByDossierId(any());
        verify(faits, never()).findByDossierId(any());
    }
}
