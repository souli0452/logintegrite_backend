# Etat trimestriel des personnes fichees - Plan d'implementation (backend)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generer et archiver chaque trimestre un etat officiel (PDF + Excel) des personnes fichees, avec synthese du trimestre, telechargeable par l'administrateur et le validateur.

**Architecture:** Table d'archive immuable `audit.etat_trimestriel` (JDBC, comme `demande_export`), un service de generation qui reutilise la lecture du registre de `RapportServiceImpl`, une tache planifiee quotidienne qui genere l'etat du trimestre ecoule s'il manque, un controleur REST. Le frontend fera l'objet d'un second plan, une fois le backend livre.

**Tech Stack:** Java 17, Spring Boot 4, JdbcTemplate, PDFBox, POI (SXSSF), JUnit 5 + Mockito + AssertJ, Flyway.

**Spec:** `docs/superpowers/specs/2026-10-06-etat-trimestriel-design.md`

## Global Constraints
- Package racine `bf.gov.ascelc.logintegrite_backend`; textes et identifiants metier en francais sans accents dans le code (comme l'existant).
- Les PDF utilisent `PdfEcriture` (jamais de PDF tronque); les Excel utilisent `SXSSFWorkbook` et `MAX_LIGNES_EXPORT`.
- Tout endpoint de ce plan porte un `@PreAuthorize` (test `AutorisationsControleursTest`).
- Lecture: ADMIN et VALIDATEUR. Generation manuelle: ADMIN seul. Consultants et agents: aucun acces.
- Une archive n'est jamais supprimee (trigger `audit.interdire_modification`), seulement marquee `remplace`.
- Corrections au spec: (1) l'empreinte SHA-256 est stockee et affichee dans la liste, pas imprimee dans le PDF (impossible: auto-reference); (2) la liste des personnes reflete le registre **au moment de la generation** (le statut d'ancrage est calcule en direct); le PDF affiche donc « Date d'arret » et « Etabli le » separement.

## Review Focus
- Trimestre 1 : precedent = T4 de l'annee precedente; fin du T4 = 31/12 (annee bissextile et non).
- Premier etat (aucun etat precedent) : pas de « nouveaux/sortis » calcules, mention « premier etat ».
- Registre vide : le PDF et l'Excel se generent sans erreur (0 personne).
- Regeneration du meme trimestre : l'ancien est conserve `remplace = true`, un seul actif.
- La tache quotidienne est idempotente : deux executions le meme jour ne creent qu'un seul etat.
- Plus de `MAX_LIGNES_EXPORT` personnes : erreur claire, aucune archive partielle.

---

### Task 1: Calcul du trimestre et migration V13

**Files:**
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/PeriodeTrimestre.java`
- Create: `src/main/resources/db/migration/V13__etat_trimestriel.sql`
- Test: `src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/PeriodeTrimestreTest.java`

**Interfaces:**
- Produces: `record PeriodeTrimestre(int annee, int trimestre)` avec `static de(LocalDate)`, `debut()`, `fin()`, `precedent()`, `libelle()` (« T3 2026 »).

- [ ] **Step 1: Ecrire le test qui echoue**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodeTrimestreTest {

    @Test
    void deduitLeTrimestreDUneDate() {
        assertThat(PeriodeTrimestre.de(LocalDate.of(2026, 1, 1))).isEqualTo(new PeriodeTrimestre(2026, 1));
        assertThat(PeriodeTrimestre.de(LocalDate.of(2026, 3, 31))).isEqualTo(new PeriodeTrimestre(2026, 1));
        assertThat(PeriodeTrimestre.de(LocalDate.of(2026, 10, 6))).isEqualTo(new PeriodeTrimestre(2026, 4));
    }

    @Test
    void bornesDuTrimestre() {
        PeriodeTrimestre t3 = new PeriodeTrimestre(2026, 3);
        assertThat(t3.debut()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(t3.fin()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(new PeriodeTrimestre(2024, 1).fin()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(new PeriodeTrimestre(2026, 4).fin()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void precedentTraverseLeChangementDAnnee() {
        assertThat(new PeriodeTrimestre(2026, 1).precedent()).isEqualTo(new PeriodeTrimestre(2025, 4));
        assertThat(new PeriodeTrimestre(2026, 3).precedent()).isEqualTo(new PeriodeTrimestre(2026, 2));
    }

    @Test
    void libelleEtValidation() {
        assertThat(new PeriodeTrimestre(2026, 3).libelle()).isEqualTo("T3 2026");
        assertThatThrownBy(() -> new PeriodeTrimestre(2026, 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PeriodeTrimestre(2026, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Lancer le test, verifier l'echec**

Run: `./mvnw -q test -Dtest=PeriodeTrimestreTest`
Expected: FAIL (compilation: `PeriodeTrimestre` introuvable).

- [ ] **Step 3: Implementer**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import java.time.LocalDate;

/** Un trimestre civil (T1 = janvier-mars ... T4 = octobre-decembre). */
public record PeriodeTrimestre(int annee, int trimestre) {

    public PeriodeTrimestre {
        if (trimestre < 1 || trimestre > 4) {
            throw new IllegalArgumentException("Trimestre invalide : " + trimestre);
        }
    }

    public static PeriodeTrimestre de(LocalDate date) {
        return new PeriodeTrimestre(date.getYear(), (date.getMonthValue() - 1) / 3 + 1);
    }

    public LocalDate debut() {
        return LocalDate.of(annee, (trimestre - 1) * 3 + 1, 1);
    }

    /** Dernier jour du trimestre : la date d'arret de l'etat. */
    public LocalDate fin() {
        return debut().plusMonths(3).minusDays(1);
    }

    public PeriodeTrimestre precedent() {
        return trimestre == 1 ? new PeriodeTrimestre(annee - 1, 4) : new PeriodeTrimestre(annee, trimestre - 1);
    }

    public String libelle() {
        return "T" + trimestre + " " + annee;
    }
}
```

- [ ] **Step 4: Ecrire la migration `V13__etat_trimestriel.sql`**

```sql
-- V13 : archive des etats trimestriels des personnes fichees remis a la hierarchie.
-- Un etat n'est jamais supprime : une regeneration marque l'ancien "remplace" et le conserve.

CREATE TABLE audit.etat_trimestriel (
    id               uuid PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    annee            smallint NOT NULL,
    trimestre        smallint NOT NULL CHECK (trimestre BETWEEN 1 AND 4),
    date_arret       date NOT NULL,
    date_generation  timestamptz NOT NULL DEFAULT now(),
    genere_par       varchar(150) NOT NULL,
    nb_personnes     integer NOT NULL,
    personne_ids     uuid[] NOT NULL,
    pdf              bytea NOT NULL,
    excel            bytea NOT NULL,
    sha256_pdf       char(64) NOT NULL,
    sha256_excel     char(64) NOT NULL,
    remplace         boolean NOT NULL DEFAULT false
);

-- un seul etat actif par trimestre
CREATE UNIQUE INDEX uq_etat_trimestriel_actif ON audit.etat_trimestriel (annee, trimestre) WHERE NOT remplace;

CREATE TRIGGER trg_etat_trimestriel_pas_de_suppression
    BEFORE DELETE ON audit.etat_trimestriel
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_etat_trimestriel_pas_de_truncate
    BEFORE TRUNCATE ON audit.etat_trimestriel
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'logintegrite_app') THEN
        REVOKE DELETE, TRUNCATE ON audit.etat_trimestriel FROM logintegrite_app;
    END IF;
END
$$;
```

- [ ] **Step 5: Lancer le test, verifier le succes**

Run: `./mvnw -q test -Dtest=PeriodeTrimestreTest`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat src/main/resources/db/migration/V13__etat_trimestriel.sql
git commit -m "feat(etat-trimestriel): calcul du trimestre et table d'archive V13"
```

---

### Task 2: Lecture du registre reutilisable et synthese

**Files:**
- Modify: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/service/RapportService.java`
- Modify: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/service/impl/RapportServiceImpl.java` (record `LigneRegistre` et `genererPdfRegistreOfficiel`)
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/SyntheseTrimestre.java`
- Test: `src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/SyntheseTrimestreTest.java`

**Interfaces:**
- Produces: `RapportService.lireRegistre(): List<RapportService.LigneRegistre>` ; `record RapportService.LigneRegistre(UUID personneId, String nom, String type, long dossiers)` ; `record SyntheseTrimestre(boolean premierEtat, List<String> nouveaux, List<String> sortis, long changementsStatut, long dossiersClos)` avec `static SyntheseTrimestre calculer(Set<UUID> precedents, List<LigneRegistre> actuel, long changementsStatut, long dossiersClos)` (`precedents == null` = premier etat).

- [ ] **Step 1: Ecrire le test qui echoue**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class SyntheseTrimestreTest {

    private static LigneRegistre ligne(UUID id, String nom) {
        return new LigneRegistre(id, nom, "PHYSIQUE", 1);
    }

    @Test
    void premierEtatNeCalculePasDeDifference() {
        SyntheseTrimestre s = SyntheseTrimestre.calculer(null, List.of(ligne(UUID.randomUUID(), "A")), 3, 2);
        assertThat(s.premierEtat()).isTrue();
        assertThat(s.nouveaux()).isEmpty();
        assertThat(s.sortis()).isEmpty();
        assertThat(s.changementsStatut()).isEqualTo(3);
        assertThat(s.dossiersClos()).isEqualTo(2);
    }

    @Test
    void differenceAvecLEtatPrecedent() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        SyntheseTrimestre s = SyntheseTrimestre.calculer(Set.of(a, b),
                List.of(ligne(b, "B"), ligne(c, "C")), 0, 0);
        assertThat(s.premierEtat()).isFalse();
        assertThat(s.nouveaux()).containsExactly("C");
        assertThat(s.sortis()).containsExactly(a.toString()); // nom inconnu : identifiant
    }

    @Test
    void registreVideSansPrecedent() {
        SyntheseTrimestre s = SyntheseTrimestre.calculer(Set.of(), List.of(), 0, 0);
        assertThat(s.nouveaux()).isEmpty();
        assertThat(s.sortis()).isEmpty();
    }
}
```

- [ ] **Step 2: Lancer, verifier l'echec**

Run: `./mvnw -q test -Dtest=SyntheseTrimestreTest`
Expected: FAIL (compilation).

- [ ] **Step 3: Implementer**

Dans `RapportService`, ajouter (avec `import java.util.List;`) :

```java
    List<LigneRegistre> lireRegistre();

    record LigneRegistre(UUID personneId, String nom, String type, long dossiers) { }
```

Dans `RapportServiceImpl` : supprimer le `private record LigneRegistre(...)` (le record de l'interface est visible directement), puis extraire la lecture :

```java
    @Override
    public List<LigneRegistre> lireRegistre() {
        PersonneSearchCriteria criteres = new PersonneSearchCriteria();
        criteres.setStatutAncrage(StatutAncrage.REGISTRE_OFFICIEL);
        Map<UUID, Long> dossiersValides = compterParCle(
                implicationRepository.compterDossiersEntierementValidesParPersonne(StatutValidation.VALIDEE));
        List<LigneRegistre> lignes = new ArrayList<>();
        lirePersonnes(criteres, p -> {
            Long nb = dossiersValides.get(p.getId());
            if (nb != null && nb > 0) {
                lignes.add(new LigneRegistre(p.getId(), p.getNomAffichage(), p.getTypePersonne().name(), nb));
            }
        });
        return lignes;
    }
```

et dans `genererPdfRegistreOfficiel()` remplacer les lignes qui construisent `criteres`, `dossiersValides`, `lignes` et appellent `lirePersonnes` par `List<LigneRegistre> lignes = lireRegistre();`.

`SyntheseTrimestre.java` :

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ce qui a bouge depuis l'etat precedent. */
public record SyntheseTrimestre(boolean premierEtat, List<String> nouveaux, List<String> sortis,
                                long changementsStatut, long dossiersClos) {

    /** @param precedents identifiants du registre a l'etat precedent, ou null s'il n'y en a pas (premier etat). */
    public static SyntheseTrimestre calculer(Set<UUID> precedents, List<LigneRegistre> actuel,
                                             long changementsStatut, long dossiersClos) {
        if (precedents == null) {
            return new SyntheseTrimestre(true, List.of(), List.of(), changementsStatut, dossiersClos);
        }
        Set<UUID> actuels = actuel.stream().map(LigneRegistre::personneId).collect(Collectors.toSet());
        List<String> nouveaux = actuel.stream()
                .filter(l -> !precedents.contains(l.personneId()))
                .map(LigneRegistre::nom).toList();
        List<String> sortis = precedents.stream()
                .filter(id -> !actuels.contains(id))
                .map(UUID::toString).toList();
        return new SyntheseTrimestre(false, nouveaux, sortis, changementsStatut, dossiersClos);
    }
}
```

- [ ] **Step 4: Lancer les tests du module rapport**

Run: `./mvnw -q test -Dtest='SyntheseTrimestreTest,RapportServiceImplTest'`
Expected: PASS (le test existant `RapportServiceImplTest` reste vert).

- [ ] **Step 5: Commit**

```bash
git add -A src/main/java/bf/gov/ascelc/logintegrite_backend/rapport src/test/java/bf/gov/ascelc/logintegrite_backend/rapport
git commit -m "feat(etat-trimestriel): lecture du registre reutilisable et synthese du trimestre"
```

---

### Task 3: Documents PDF et Excel de l'etat

**Files:**
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielDocuments.java`
- Test: `src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielDocumentsTest.java`

**Interfaces:**
- Consumes: `PeriodeTrimestre`, `SyntheseTrimestre`, `LigneRegistre`, `PdfEcriture(PDDocument, String)` avec `titre/ligne/ligneGrasse/espace/ligneTableau/terminer`.
- Produces: `static byte[] pdf(PeriodeTrimestre, LocalDate etabliLe, SyntheseTrimestre, List<LigneRegistre>)` ; `static byte[] excel(...)` (memes parametres).

- [ ] **Step 1: Ecrire le test qui echoue**

```java
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
```

- [ ] **Step 2: Lancer, verifier l'echec**

Run: `./mvnw -q test -Dtest=EtatTrimestrielDocumentsTest`
Expected: FAIL (compilation).

- [ ] **Step 3: Implementer**

```java
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
                synthese.nouveaux().forEach(n -> pdf.ligne("  + " + n));
                pdf.ligne("Personnes sorties du registre : " + synthese.sortis().size());
                synthese.sortis().forEach(n -> pdf.ligne("  - " + n));
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
```
Si `PdfEcriture` n'est pas `public` (classe et constructeur), les rendre `public` dans le meme commit.

- [ ] **Step 4: Lancer le test**

Run: `./mvnw -q test -Dtest=EtatTrimestrielDocumentsTest`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add -A src/main/java/bf/gov/ascelc/logintegrite_backend/rapport src/test/java/bf/gov/ascelc/logintegrite_backend/rapport
git commit -m "feat(etat-trimestriel): documents PDF et Excel de l'etat"
```

---

### Task 4: Archive (repository JDBC) et service de generation

**Files:**
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielRepository.java`
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielService.java`
- Test: `src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielServiceTest.java`

**Interfaces:**
- Consumes: `RapportService.lireRegistre()`, `EtatTrimestrielDocuments.pdf/excel`, `SyntheseTrimestre.calculer`, `PeriodeTrimestre`.
- Produces:
  - `EtatTrimestrielRepository.EtatTrimestrielResume(UUID id, int annee, int trimestre, LocalDate dateArret, Instant dateGeneration, String generePar, int nbPersonnes, String sha256Pdf, String sha256Excel, boolean remplace)`
  - `EtatTrimestrielRepository` (`@Repository`, constructeur `JdbcTemplate`): `boolean existeActif(PeriodeTrimestre)`, `Set<UUID> idsDernierEtatAvant(PeriodeTrimestre)` (null si aucun), `long compterChangementsStatut(LocalDate debut, LocalDate fin)`, `long compterDossiersClos(LocalDate debut, LocalDate fin)`, `void archiver(PeriodeTrimestre, String generePar, List<UUID> ids, byte[] pdf, byte[] excel, String shaPdf, String shaExcel)` (marque l'ancien actif `remplace` puis insere), `List<EtatTrimestrielResume> lister()`, `Optional<byte[]> pdf(UUID)`, `Optional<byte[]> excel(UUID)`.
  - `EtatTrimestrielService` (`@Service`, constructeur `(RapportService, EtatTrimestrielRepository)`): `void generer(PeriodeTrimestre, String generePar, LocalDate aujourdhui)` ; `boolean genererSiManquant(LocalDate aujourdhui)` (cible = trimestre precedent d'aujourd'hui ; ne fait rien si `existeActif` ; renvoie true si genere).

- [ ] **Step 1: Ecrire le test qui echoue**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EtatTrimestrielServiceTest {

    private final RapportService rapports = mock(RapportService.class);
    private final EtatTrimestrielRepository repo = mock(EtatTrimestrielRepository.class);
    private final EtatTrimestrielService service = new EtatTrimestrielService(rapports, repo);
    private static final PeriodeTrimestre T3 = new PeriodeTrimestre(2026, 3);

    @Test
    void genereEtArchiveAvecEmpreintesEtSynthese() {
        UUID a = UUID.randomUUID();
        when(rapports.lireRegistre()).thenReturn(List.of(new LigneRegistre(a, "Alice", "PHYSIQUE", 1)));
        when(repo.idsDernierEtatAvant(T3)).thenReturn(null);
        when(repo.compterChangementsStatut(T3.debut(), T3.fin())).thenReturn(2L);
        when(repo.compterDossiersClos(T3.debut(), T3.fin())).thenReturn(1L);

        service.generer(T3, "SYSTEME", LocalDate.of(2026, 10, 6));

        ArgumentCaptor<byte[]> pdf = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<String> sha = ArgumentCaptor.forClass(String.class);
        verify(repo).archiver(eq(T3), eq("SYSTEME"), eq(List.of(a)), pdf.capture(), any(), sha.capture(), anyString());
        assertThat(pdf.getValue()).isNotEmpty();
        assertThat(sha.getValue()).matches("[0-9a-f]{64}");
    }

    @Test
    void registreVideEstArchiveSansErreur() {
        when(rapports.lireRegistre()).thenReturn(List.of());
        service.generer(T3, "SYSTEME", LocalDate.of(2026, 10, 6));
        verify(repo).archiver(eq(T3), eq("SYSTEME"), eq(List.of()), any(), any(), anyString(), anyString());
    }

    @Test
    void erreurDeLectureNArchiveRien() {
        when(rapports.lireRegistre()).thenThrow(new IllegalArgumentException("Export limite"));
        assertThatThrownBy(() -> service.generer(T3, "SYSTEME", LocalDate.of(2026, 10, 6)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).archiver(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void genererSiManquantCibleLeTrimestrePrecedent() {
        when(repo.existeActif(T3)).thenReturn(false);
        when(rapports.lireRegistre()).thenReturn(List.of());
        assertThat(service.genererSiManquant(LocalDate.of(2026, 10, 6))).isTrue();
        verify(repo).archiver(eq(T3), eq("SYSTEME"), any(), any(), any(), anyString(), anyString());
    }

    @Test
    void genererSiManquantEstIdempotent() {
        when(repo.existeActif(T3)).thenReturn(true);
        assertThat(service.genererSiManquant(LocalDate.of(2026, 10, 6))).isFalse();
        verify(repo, never()).archiver(any(), any(), any(), any(), any(), any(), any());
    }
}
```

- [ ] **Step 2: Lancer, verifier l'echec**

Run: `./mvnw -q test -Dtest=EtatTrimestrielServiceTest`
Expected: FAIL (compilation).

- [ ] **Step 3: Implementer le repository**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EtatTrimestrielRepository {

    private final JdbcTemplate jdbc;

    public record EtatTrimestrielResume(UUID id, int annee, int trimestre, LocalDate dateArret,
                                        Instant dateGeneration, String generePar, int nbPersonnes,
                                        String sha256Pdf, String sha256Excel, boolean remplace) { }

    public boolean existeActif(PeriodeTrimestre p) {
        Integer n = jdbc.queryForObject(
                "select count(*) from audit.etat_trimestriel where annee = ? and trimestre = ? and not remplace",
                Integer.class, p.annee(), p.trimestre());
        return n != null && n > 0;
    }

    /** Identifiants du registre au dernier etat actif strictement anterieur a la periode ; null s'il n'y en a pas. */
    public Set<UUID> idsDernierEtatAvant(PeriodeTrimestre p) {
        List<Set<UUID>> r = jdbc.query(
                "select personne_ids from audit.etat_trimestriel where not remplace "
                        + "and (annee < ? or (annee = ? and trimestre < ?)) order by annee desc, trimestre desc limit 1",
                (rs, i) -> {
                    Array a = rs.getArray(1);
                    Set<UUID> ids = new HashSet<>();
                    for (Object o : (Object[]) a.getArray()) ids.add((UUID) o);
                    return ids;
                }, p.annee(), p.annee(), p.trimestre());
        return r.isEmpty() ? null : r.get(0);
    }

    public long compterChangementsStatut(LocalDate debut, LocalDate fin) {
        Long n = jdbc.queryForObject(
                "select count(*) from audit.journal_audit where action = 'MODIFICATION_STATUT_JUDICIAIRE' "
                        + "and entite_cible = 'ImplicationFait' and date_action >= ?::date "
                        + "and date_action < (?::date + 1)",
                Long.class, debut, fin);
        return n == null ? 0 : n;
    }

    public long compterDossiersClos(LocalDate debut, LocalDate fin) {
        Long n = jdbc.queryForObject(
                "select count(*) from dossiers.dossier where date_cloture between ? and ?", Long.class, debut, fin);
        return n == null ? 0 : n;
    }

    @Transactional
    public void archiver(PeriodeTrimestre p, String generePar, List<UUID> ids, byte[] pdf, byte[] excel,
                         String shaPdf, String shaExcel) {
        jdbc.update("update audit.etat_trimestriel set remplace = true where annee = ? and trimestre = ? and not remplace",
                p.annee(), p.trimestre());
        jdbc.update("insert into audit.etat_trimestriel (annee, trimestre, date_arret, genere_par, nb_personnes, "
                        + "personne_ids, pdf, excel, sha256_pdf, sha256_excel) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, p.annee());
                    ps.setInt(2, p.trimestre());
                    ps.setObject(3, p.fin());
                    ps.setString(4, generePar);
                    ps.setInt(5, ids.size());
                    ps.setArray(6, ps.getConnection().createArrayOf("uuid", ids.toArray()));
                    ps.setBytes(7, pdf);
                    ps.setBytes(8, excel);
                    ps.setString(9, shaPdf);
                    ps.setString(10, shaExcel);
                });
    }

    public List<EtatTrimestrielResume> lister() {
        return jdbc.query("select id, annee, trimestre, date_arret, date_generation, genere_par, nb_personnes, "
                        + "sha256_pdf, sha256_excel, remplace from audit.etat_trimestriel "
                        + "order by annee desc, trimestre desc, date_generation desc",
                (rs, i) -> new EtatTrimestrielResume(rs.getObject("id", UUID.class), rs.getInt("annee"),
                        rs.getInt("trimestre"), rs.getObject("date_arret", LocalDate.class),
                        rs.getTimestamp("date_generation").toInstant(), rs.getString("genere_par"),
                        rs.getInt("nb_personnes"), rs.getString("sha256_pdf"), rs.getString("sha256_excel"),
                        rs.getBoolean("remplace")));
    }

    public Optional<byte[]> pdf(UUID id) { return octets("pdf", id); }

    public Optional<byte[]> excel(UUID id) { return octets("excel", id); }

    private Optional<byte[]> octets(String colonne, UUID id) {
        List<byte[]> r = jdbc.query("select " + colonne + " from audit.etat_trimestriel where id = ?",
                (rs, i) -> rs.getBytes(1), id);
        return r.stream().findFirst();
    }
}
```

- [ ] **Step 4: Implementer le service**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService;
import bf.gov.ascelc.logintegrite_backend.rapport.service.RapportService.LigneRegistre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtatTrimestrielService {

    private final RapportService rapports;
    private final EtatTrimestrielRepository repository;

    /** Genere et archive l'etat d'un trimestre ; rien n'est archive si une etape echoue. */
    public void generer(PeriodeTrimestre periode, String generePar, LocalDate aujourdhui) {
        List<LigneRegistre> lignes = rapports.lireRegistre();
        Set<UUID> precedents = repository.idsDernierEtatAvant(periode);
        SyntheseTrimestre synthese = SyntheseTrimestre.calculer(precedents, lignes,
                repository.compterChangementsStatut(periode.debut(), periode.fin()),
                repository.compterDossiersClos(periode.debut(), periode.fin()));
        byte[] pdf = EtatTrimestrielDocuments.pdf(periode, aujourdhui, synthese, lignes);
        byte[] excel = EtatTrimestrielDocuments.excel(periode, aujourdhui, synthese, lignes);
        repository.archiver(periode, generePar, lignes.stream().map(LigneRegistre::personneId).toList(),
                pdf, excel, sha256(pdf), sha256(excel));
        log.info("Etat trimestriel {} archive ({} personnes)", periode.libelle(), lignes.size());
    }

    /** Genere l'etat du trimestre ecoule s'il n'existe pas encore. */
    public boolean genererSiManquant(LocalDate aujourdhui) {
        PeriodeTrimestre cible = PeriodeTrimestre.de(aujourdhui).precedent();
        if (repository.existeActif(cible)) return false;
        generer(cible, "SYSTEME", aujourdhui);
        return true;
    }

    static String sha256(byte[] octets) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(octets));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
```

- [ ] **Step 5: Lancer le test**

Run: `./mvnw -q test -Dtest=EtatTrimestrielServiceTest`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
git add -A src/main/java/bf/gov/ascelc/logintegrite_backend/rapport src/test/java/bf/gov/ascelc/logintegrite_backend/rapport
git commit -m "feat(etat-trimestriel): archive JDBC et service de generation"
```

---

### Task 5: Tache planifiee

**Files:**
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielJob.java`
- Test: `src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielJobTest.java`

**Interfaces:**
- Consumes: `EtatTrimestrielService.genererSiManquant(LocalDate)`.

- [ ] **Step 1: Test qui echoue**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EtatTrimestrielJobTest {

    private final EtatTrimestrielService service = mock(EtatTrimestrielService.class);
    private final EtatTrimestrielJob job = new EtatTrimestrielJob(service);

    @Test
    void delegueAuService() {
        job.verifier();
        verify(service).genererSiManquant(any());
    }

    @Test
    void uneErreurNeFaitPasPlanterLaTache() {
        when(service.genererSiManquant(any())).thenThrow(new IllegalStateException("base indisponible"));
        assertThatCode(job::verifier).doesNotThrowAnyException();
    }
}
```

- [ ] **Step 2: Lancer, verifier l'echec**

Run: `./mvnw -q test -Dtest=EtatTrimestrielJobTest`
Expected: FAIL (compilation).

- [ ] **Step 3: Implementer**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Verifie chaque jour que l'etat du trimestre ecoule existe, et le genere sinon. Couvre a la fois la generation
 * normale (le 1er jour du trimestre suivant) et le rattrapage apres une indisponibilite du serveur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EtatTrimestrielJob {

    private final EtatTrimestrielService service;

    @Scheduled(initialDelay = 180_000, fixedDelay = 86_400_000)
    public void verifier() {
        try {
            service.genererSiManquant(LocalDate.now());
        } catch (RuntimeException e) {
            // Reessaye au prochain passage ; ne bloque jamais le demarrage.
            log.warn("Generation de l'etat trimestriel impossible : {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 4: Lancer le test**

Run: `./mvnw -q test -Dtest=EtatTrimestrielJobTest`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add -A src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat
git commit -m "feat(etat-trimestriel): tache quotidienne de generation et de rattrapage"
```

---

### Task 6: Endpoints REST et droits

**Files:**
- Create: `src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielController.java`
- Test: `src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat/EtatTrimestrielControllerTest.java`

**Interfaces:**
- Consumes: `EtatTrimestrielRepository.lister/pdf/excel`, `EtatTrimestrielService.generer`, `CurrentUserProvider.utilisateurCourant()` (`Utilisateur.getPrenom()/getNom()`), `ResourceNotFoundException(String, UUID)`.
- Produces: `GET /api/v1/rapports/etats-trimestriels`, `GET /{id}/pdf`, `GET /{id}/excel`, `POST /generer?annee=&trimestre=` (ADMIN).

- [ ] **Step 1: Test qui echoue**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtatTrimestrielControllerTest {

    private final EtatTrimestrielRepository repo = mock(EtatTrimestrielRepository.class);
    private final EtatTrimestrielService service = mock(EtatTrimestrielService.class);
    private final CurrentUserProvider courant = mock(CurrentUserProvider.class);
    private final EtatTrimestrielController controller = new EtatTrimestrielController(repo, service, courant);

    @Test
    void lectureReserveeAuValidateurEtALAdmin() {
        PreAuthorize p = AnnotatedElementUtils.findMergedAnnotation(EtatTrimestrielController.class, PreAuthorize.class);
        assertThat(p.value()).contains("VALIDATEUR").contains("ADMIN").doesNotContain("AGENT").doesNotContain("CONSULT");
    }

    @Test
    void generationManuelleReserveeALAdmin() throws Exception {
        PreAuthorize p = AnnotatedElementUtils.findMergedAnnotation(
                EtatTrimestrielController.class.getMethod("generer", int.class, int.class), PreAuthorize.class);
        assertThat(p.value()).isEqualTo("hasRole('ADMIN')");
    }

    @Test
    void pdfInconnuDonne404() {
        UUID id = UUID.randomUUID();
        when(repo.pdf(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.pdf(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void pdfConnuEstRenvoye() {
        UUID id = UUID.randomUUID();
        when(repo.pdf(id)).thenReturn(Optional.of(new byte[]{1, 2}));
        assertThat(controller.pdf(id).getBody()).containsExactly(1, 2);
    }

    @Test
    void trimestreNonTermineEstRefuse() {
        int annee = java.time.LocalDate.now().getYear() + 1;
        assertThatThrownBy(() -> controller.generer(annee, 1)).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Lancer, verifier l'echec**

Run: `./mvnw -q test -Dtest=EtatTrimestrielControllerTest`
Expected: FAIL (compilation).

- [ ] **Step 3: Implementer**

```java
package bf.gov.ascelc.logintegrite_backend.rapport.etat;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.rapport.etat.EtatTrimestrielRepository.EtatTrimestrielResume;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rapports/etats-trimestriels")
@PreAuthorize("hasAnyRole('VALIDATEUR','ADMIN')")
@RequiredArgsConstructor
public class EtatTrimestrielController {

    private final EtatTrimestrielRepository repository;
    private final EtatTrimestrielService service;
    private final CurrentUserProvider utilisateurCourant;

    @GetMapping
    public List<EtatTrimestrielResume> lister() {
        return repository.lister();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
        byte[] pdf = repository.pdf(id).orElseThrow(() -> new ResourceNotFoundException("Etat trimestriel", id));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"etat-trimestriel-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable UUID id) {
        byte[] excel = repository.excel(id).orElseThrow(() -> new ResourceNotFoundException("Etat trimestriel", id));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"etat-trimestriel-" + id + ".xlsx\"")
                .body(excel);
    }

    @PostMapping("/generer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> generer(@RequestParam int annee, @RequestParam int trimestre) {
        PeriodeTrimestre periode = new PeriodeTrimestre(annee, trimestre);
        if (periode.fin().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Le trimestre " + periode.libelle() + " n'est pas encore termine.");
        }
        Utilisateur u = utilisateurCourant.utilisateurCourant();
        service.generer(periode, u.getPrenom() + " " + u.getNom(), LocalDate.now());
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 4: Lancer ce test puis toute la suite**

Run: `./mvnw -q test -Dtest=EtatTrimestrielControllerTest` puis `./mvnw -q test`
Expected: PASS partout (dont `AutorisationsControleursTest`).

- [ ] **Step 5: Commit**

```bash
git add -A src/main/java/bf/gov/ascelc/logintegrite_backend/rapport/etat src/test/java/bf/gov/ascelc/logintegrite_backend/rapport/etat
git commit -m "feat(etat-trimestriel): endpoints de liste, telechargement et generation manuelle"
```

---

### Task 7: Verification sur base reelle (manuelle)

- [ ] **Step 1:** Demarrer le backend en dev : `./mvnw spring-boot:run`. Verifier dans les logs `Migrating schema "core" to version "13 - etat trimestriel"`.
- [ ] **Step 2:** Se connecter en ADMIN, `POST /api/v1/rapports/etats-trimestriels/generer?annee=2026&trimestre=3`, attendre 204.
- [ ] **Step 3:** `GET /api/v1/rapports/etats-trimestriels` : une ligne, `remplace=false`. Telecharger le PDF et l'Excel, les ouvrir.
- [ ] **Step 4:** Relancer la generation du meme trimestre : la liste montre 2 lignes, une seule `remplace=false`.
- [ ] **Step 5:** Avec un compte AGENT puis CONSULTANT : `GET` renvoie 403.
- [ ] **Step 6:** Pousser et deployer comme le V12 (sauvegarde `pg_dump` verifiee d'abord, puis `up -d --build backend`).

## Suite
Second plan, apres livraison du backend : page frontend « Etats trimestriels » (liste, telechargement PDF/Excel, bouton « Generer maintenant » pour l'administrateur).
