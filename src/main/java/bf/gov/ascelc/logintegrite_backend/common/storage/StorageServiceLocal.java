// common/storage/StorageServiceLocal.java
package bf.gov.ascelc.logintegrite_backend.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Remplacable par une implementation MinIO plus tard sans toucher aux
// services metier (DocumentServiceImpl ne connait que l'interface).
@Component
public class StorageServiceLocal implements StorageService {

    @Value("${logintegrite.stockage.repertoire:./storage/documents}")
    private String repertoireBase;

    @Override
    public String stocker(MultipartFile fichier, String nomStockage) throws IOException {
        Path dossier = Path.of(repertoireBase);
        Files.createDirectories(dossier);
        Path cible = dossier.resolve(nomStockage);
        Files.copy(fichier.getInputStream(), cible, StandardCopyOption.REPLACE_EXISTING);
        return cible.toAbsolutePath().toString();
    }

    @Override
    public Resource recuperer(String cheminStockage) {
        return new FileSystemResource(cheminStockage);
    }
}
