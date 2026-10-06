package bf.gov.ascelc.logintegrite_backend.rapport.service.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecriture de texte dans un PDF avec saut de page automatique, retour a la ligne et neutralisation
 * des caracteres que les polices standard ne savent pas afficher (qui feraient echouer PDFBox).
 * Un rapport n'est jamais tronque : quand la page est pleine, une nouvelle page est ouverte.
 */
public final class PdfEcriture implements AutoCloseable {

    private static final float MARGE = 50f;
    private static final float HAUT = 790f;
    private static final float BAS = 60f;
    private static final float LARGEUR_UTILE = PDRectangle.A4.getWidth() - 2 * MARGE;

    private final PDDocument document;
    private final String enTete;
    private final PDFont normale = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont grasse = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDFont monospace = new PDType1Font(Standard14Fonts.FontName.COURIER);

    private PDPageContentStream flux;
    private float y;
    private int pages;

    public PdfEcriture(PDDocument document, String enTete) throws IOException {
        this.document = document;
        this.enTete = enTete;
        nouvellePage();
    }

    int nombrePages() {
        return pages;
    }

    public void titre(String texte) throws IOException {
        ecrire(texte, grasse, 16, 26);
    }

    public void ligne(String texte) throws IOException {
        ecrire(texte, normale, 11, 16);
    }

    public void ligneGrasse(String texte) throws IOException {
        ecrire(texte, grasse, 11, 16);
    }

    /** Ligne a chasse fixe : colonnes alignees pour les tableaux. */
    public void ligneTableau(String texte) throws IOException {
        ecrire(texte, monospace, 9, 13);
    }

    public void espace(float hauteur) throws IOException {
        y -= hauteur;
        if (y < BAS) nouvellePage();
    }

    /** Ajoute "Page i / n" en pied de chaque page puis ferme le flux courant. */
    public void terminer() throws IOException {
        flux.close();
        flux = null;
        int total = document.getNumberOfPages();
        for (int i = 0; i < total; i++) {
            try (PDPageContentStream pied = new PDPageContentStream(
                    document, document.getPage(i), PDPageContentStream.AppendMode.APPEND, true, true)) {
                pied.beginText();
                pied.setFont(normale, 8);
                pied.newLineAtOffset(MARGE, 30);
                pied.showText(assainir("Page " + (i + 1) + " / " + total + "  -  " + enTete, normale));
                pied.endText();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (flux != null) {
            flux.close();
            flux = null;
        }
    }

    // -------------------------------------------------------------------------

    private void ecrire(String texte, PDFont police, float taille, float interligne) throws IOException {
        for (String segment : decouper(assainir(texte, police), police, taille)) {
            if (y - interligne < BAS) nouvellePage();
            flux.beginText();
            flux.setFont(police, taille);
            flux.newLineAtOffset(MARGE, y);
            flux.showText(segment);
            flux.endText();
            y -= interligne;
        }
    }

    private void nouvellePage() throws IOException {
        if (flux != null) flux.close();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        pages++;
        flux = new PDPageContentStream(document, page);
        y = HAUT;
    }

    /** Coupe le texte aux mots pour qu'aucune ligne ne depasse la largeur utile de la page. */
    private List<String> decouper(String texte, PDFont police, float taille) throws IOException {
        List<String> lignes = new ArrayList<>();
        StringBuilder courante = new StringBuilder();
        for (String mot : texte.split(" ", -1)) {
            String essai = courante.length() == 0 ? mot : courante + " " + mot;
            if (largeur(essai, police, taille) <= LARGEUR_UTILE) {
                courante.setLength(0);
                courante.append(essai);
                continue;
            }
            if (courante.length() > 0) {
                lignes.add(courante.toString());
                courante.setLength(0);
            }
            // Mot plus long qu'une ligne entiere : decoupe caractere par caractere.
            String reste = mot;
            while (largeur(reste, police, taille) > LARGEUR_UTILE) {
                int coupe = reste.length();
                while (coupe > 1 && largeur(reste.substring(0, coupe), police, taille) > LARGEUR_UTILE) coupe--;
                lignes.add(reste.substring(0, coupe));
                reste = reste.substring(coupe);
            }
            courante.append(reste);
        }
        lignes.add(courante.toString());
        return lignes;
    }

    private float largeur(String texte, PDFont police, float taille) throws IOException {
        return police.getStringWidth(texte) / 1000f * taille;
    }

    /** Remplace retours a la ligne/tabulations par des espaces et les caracteres non affichables par '?'. */
    static String assainir(String texte, PDFont police) {
        if (texte == null) return "-";
        StringBuilder sb = new StringBuilder(texte.length());
        texte.codePoints().forEach(cp -> {
            if (cp == '\n' || cp == '\r' || cp == '\t') {
                sb.append(' ');
                return;
            }
            String c = new String(Character.toChars(cp));
            try {
                police.getStringWidth(c);
                sb.append(c);
            } catch (IllegalArgumentException | IOException e) {
                sb.append('?');
            }
        });
        return sb.toString();
    }
}
