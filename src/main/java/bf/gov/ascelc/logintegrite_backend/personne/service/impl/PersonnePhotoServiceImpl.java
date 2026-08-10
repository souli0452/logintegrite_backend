package bf.gov.ascelc.logintegrite_backend.personne.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.storage.StorageService;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonnePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonnePhotoServiceImpl implements PersonnePhotoService {

    private final PersonneRepository repository;
    private final StorageService storageService;

    @Override
    public void deposer(UUID personneId, MultipartFile fichier) {
        Personne personne = trouverOuLever(personneId);
        String nomStockage = "photo-" + UUID.randomUUID() + extraireExtension(fichier.getOriginalFilename());

        String chemin;
        try {
            chemin = storageService.stocker(fichier, nomStockage);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec du stockage de la photo", e);
        }

        personne.setPhotoNomOriginal(fichier.getOriginalFilename());
        personne.setPhotoNomStockage(nomStockage);
        personne.setPhotoCheminStockage(chemin);
        personne.setPhotoTypeMime(fichier.getContentType());
        repository.save(personne);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource recuperer(UUID personneId) {
        Personne personne = trouverOuLever(personneId);
        if (personne.getPhotoCheminStockage() == null) {
            throw new ResourceNotFoundException("Photo de la personne", personneId);
        }
        return storageService.recuperer(personne.getPhotoCheminStockage());
    }

    @Override
    @Transactional(readOnly = true)
    public String typeMime(UUID personneId) {
        return trouverOuLever(personneId).getPhotoTypeMime();
    }

    @Override
    public void supprimer(UUID personneId) {
        Personne personne = trouverOuLever(personneId);
        personne.setPhotoNomOriginal(null);
        personne.setPhotoNomStockage(null);
        personne.setPhotoCheminStockage(null);
        personne.setPhotoTypeMime(null);
        repository.save(personne);
    }

    private Personne trouverOuLever(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Personne", id));
    }

    private String extraireExtension(String nomFichier) {
        if (nomFichier == null || !nomFichier.contains(".")) return "";
        return nomFichier.substring(nomFichier.lastIndexOf('.'));
    }
}
