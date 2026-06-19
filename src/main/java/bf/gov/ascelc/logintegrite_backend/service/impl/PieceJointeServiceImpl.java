package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.PieceJointeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PieceJointeResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PieceJointe;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.PieceJointeMapper;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.PieceJointeRepository;
import bf.gov.ascelc.logintegrite_backend.service.PieceJointeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PieceJointeServiceImpl implements PieceJointeService {

    private final PieceJointeRepository repository;
    private final FicheMiseEnCauseRepository ficheRepository;
    private final PieceJointeMapper mapper;

    private final Path rootLocation = Paths.get("uploads");

    @Override
    public PieceJointeResponse create(PieceJointeRequest request) {
        PieceJointe entity = mapper.toEntity(request);

        FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));
        entity.setFiche(fiche);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public PieceJointeResponse enregistrerFichierPhysique(UUID ficheId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Impossible d'enregistrer un fichier vide.");
            }

            Files.createDirectories(rootLocation);

            String nomOriginal = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            String nomUnique = UUID.randomUUID().toString() + "_" + nomOriginal;
            Path destination = rootLocation.resolve(nomUnique);


            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);


            PieceJointeRequest request = PieceJointeRequest.builder()
                    .ficheId(ficheId)
                    .nomFichier(nomOriginal)
                    .typeFichier(file.getContentType())
                    .tailleOctets(file.getSize())
                    .urlStockage(destination.toAbsolutePath().toString())
                    .build();

            return this.create(request);

        } catch (IOException e) {
            throw new RuntimeException("Erreur critique lors de la sauvegarde physique du justificatif : " + e.getMessage(), e);
        }
    }

    @Override
    public PieceJointeResponse update(UUID id, PieceJointeRequest request) {
        PieceJointe entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe non trouvée avec l'id : " + id));

        mapper.updateEntityFromRequest(request, entity);

        if (!entity.getFiche().getId().equals(request.getFicheId())) {
            FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));
            entity.setFiche(fiche);
        }

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PieceJointeResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PieceJointeResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PieceJointeResponse> getByFicheId(UUID ficheId) {
        return repository.findByFicheId(ficheId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        PieceJointe entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pièce jointe non trouvée avec l'id : " + id));

        // Suppression physique optionnelle du fichier sur le disque pour libérer l'espace sur le serveur
        try {
            Path fichierPhysique = Paths.get(entity.getUrlStockage());
            Files.deleteIfExists(fichierPhysique);
        } catch (IOException ignored) {
            // Log de débogage si nécessaire, ne bloque pas la suppression en base
        }

        repository.delete(entity);
    }
}