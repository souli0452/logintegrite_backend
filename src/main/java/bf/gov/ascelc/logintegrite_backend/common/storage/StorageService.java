// common/storage/StorageService.java
package bf.gov.ascelc.logintegrite_backend.common.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String stocker(MultipartFile fichier, String nomStockage) throws IOException;
    Resource recuperer(String cheminStockage);
}
