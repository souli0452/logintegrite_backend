package bf.gov.ascelc.logintegrite_backend.personne.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PersonnePhotoService {
    void deposer(UUID personneId, MultipartFile fichier);
    Resource recuperer(UUID personneId);
    String typeMime(UUID personneId);
    void supprimer(UUID personneId);
}
