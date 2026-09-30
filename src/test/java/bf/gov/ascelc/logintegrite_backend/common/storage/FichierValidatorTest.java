package bf.gov.ascelc.logintegrite_backend.common.storage;

import bf.gov.ascelc.logintegrite_backend.common.exception.FichierInvalideException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FichierValidatorTest {

    private final FichierValidator validator = new FichierValidator();

    private static MockMultipartFile fichier(String nom, byte[] contenu) {
        return new MockMultipartFile("fichier", nom, "application/octet-stream", contenu);
    }

    @Test
    void accepteUnPdfValideEtRenvoieLeTypeMimeCanonique() {
        var resultat = validator.valider(fichier("Rapport.PDF", "%PDF-1.7 contenu".getBytes()));

        assertThat(resultat.extension()).isEqualTo("pdf");
        assertThat(resultat.typeMime()).isEqualTo("application/pdf");
        assertThat(resultat.nomOriginal()).isEqualTo("Rapport.PDF");
    }

    @Test
    void refuseUneExtensionNonAutorisee() {
        assertThatThrownBy(() -> validator.valider(fichier("virus.exe", "MZ".getBytes())))
                .isInstanceOf(FichierInvalideException.class)
                .hasMessageContaining("Extension non autorisee");
        assertThatThrownBy(() -> validator.valider(fichier("page.html", "<html>".getBytes())))
                .isInstanceOf(FichierInvalideException.class);
    }

    @Test
    void refuseUnFichierDontLeContenuNeCorrespondPasAExtension() {
        assertThatThrownBy(() -> validator.valider(fichier("faux.pdf", "<script>alert(1)</script>".getBytes())))
                .isInstanceOf(FichierInvalideException.class)
                .hasMessageContaining("ne correspond pas");
    }

    @Test
    void refuseUnFichierVide() {
        assertThatThrownBy(() -> validator.valider(fichier("vide.pdf", new byte[0])))
                .isInstanceOf(FichierInvalideException.class);
    }

    @Test
    void neConserveJamaisDeCheminDansLeNom() {
        assertThat(FichierValidator.nettoyerNom("../../etc/passwd.txt")).isEqualTo("passwd.txt");
        assertThat(FichierValidator.nettoyerNom("C:\\Users\\x\\photo.png")).isEqualTo("photo.png");
    }

    @Test
    void refuseLesNomsVidesOuTropLongs() {
        assertThatThrownBy(() -> FichierValidator.nettoyerNom("   ")).isInstanceOf(FichierInvalideException.class);
        assertThatThrownBy(() -> FichierValidator.nettoyerNom("a".repeat(250) + ".pdf"))
                .isInstanceOf(FichierInvalideException.class);
    }
}
