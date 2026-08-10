package bf.gov.ascelc.logintegrite_backend.personne.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PieceIdentiteRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PieceIdentiteResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PieceIdentite;
import bf.gov.ascelc.logintegrite_backend.personne.enums.TypePieceIdentite;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.PieceIdentiteMapper;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PieceIdentiteRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.PieceIdentiteService;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypePieceIdentiteRef;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypePieceIdentiteRefRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PieceIdentiteServiceImpl implements PieceIdentiteService {

    private final PieceIdentiteRepository repository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final TypePieceIdentiteRefRepository typePieceRefRepository;
    private final PieceIdentiteMapper mapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<PieceIdentiteResponse> listerParPersonnePhysique(UUID personnePhysiqueId) {
        PersonnePhysique personne = trouverOuLever(personnePhysiqueId);
        return personne.getPiecesIdentite().stream().map(mapper::toResponse).toList();
    }

    @Override
    public PieceIdentiteResponse creer(UUID personnePhysiqueId, PieceIdentiteRequest request) {

        // ─── Récupération de la personne ───────────────────────────────────────
        PersonnePhysique personne = personnePhysiqueRepository.findById(personnePhysiqueId)
            .orElseThrow(() -> new ResourceNotFoundException("Personne physique", personnePhysiqueId));

        // ─── Résolution manuelle du référentiel avant vérification d'unicité ───
        TypePieceIdentiteRef typePieceRef = null;
        TypePieceIdentite typePieceEnum = null;

        if (request.getTypePieceId() != null) {
            // Cas moderne : le front envoie l'ID du référentiel
            typePieceRef = typePieceRefRepository.findById(request.getTypePieceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Type de pièce d'identité", request.getTypePieceId()));

            try {
                typePieceEnum = TypePieceIdentite.valueOf(typePieceRef.getCode());
            } catch (IllegalArgumentException ignored) {
                // Nouveau code référentiel non présent dans l'enum legacy
            }

        } else if (request.getTypePiece() != null) {
            // Cas legacy : le front envoie l'enum
            typePieceEnum = request.getTypePiece();
            typePieceRef = typePieceRefRepository.findByCodeIgnoreCase(request.getTypePiece().name())
                .orElse(null);
        } else {
            throw new IllegalArgumentException(
                "Type de pièce d'identité obligatoire (typePieceId ou typePiece).");
        }

        // ─── Vérification d'unicité HYBRIDE ────────────────────────────────────
        boolean doublon;
        if (typePieceRef != null) {
            doublon = repository.existsByTypePieceRef_IdAndNumero(typePieceRef.getId(), request.getNumero());
        } else {
            doublon = repository.existsByTypePieceAndNumero(typePieceEnum, request.getNumero());
        }
        if (doublon) {
            throw new IllegalArgumentException(
                "Une pièce d'identité avec ce numéro existe déjà pour ce type.");
        }

        // ─── Construction de l'entité ──────────────────────────────────────────
        PieceIdentite entite = mapper.toEntity(request);
        entite.setPersonnePhysique(personne);
        entite.setTypePiece(typePieceEnum);
        entite.setTypePieceRef(typePieceRef);

        PieceIdentite sauvegarde = repository.save(entite);

        // ─── Audit ─────────────────────────────────────────────────────────────
        auditService.enregistrer("CREATION", "PieceIdentite", sauvegarde.getId(), null,
            Map.of("typePiece",
                    typePieceRef != null ? typePieceRef.getCode() : typePieceEnum.name(),
                   "numero", request.getNumero()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public PieceIdentiteResponse modifier(UUID pieceId, PieceIdentiteRequest request) {

        // ─── 1. Récupération de la pièce existante ─────────────────────────────
        PieceIdentite entite = repository.findById(pieceId)
            .orElseThrow(() -> new ResourceNotFoundException("Pièce d'identité", pieceId));

        // Capture de l'état précédent pour l'audit
        Map<String, Object> avant = Map.of(
            "typePiece", entite.getTypePieceRef() != null
                ? entite.getTypePieceRef().getCode()
                : (entite.getTypePiece() != null ? entite.getTypePiece().name() : ""),
            "numero", entite.getNumero()
        );

        // ─── 2. Résolution du nouveau type (hybride) ───────────────────────────
        TypePieceIdentiteRef nouveauRef = null;
        TypePieceIdentite nouvelEnum = null;

        if (request.getTypePieceId() != null) {
            nouveauRef = typePieceRefRepository.findById(request.getTypePieceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Type de pièce d'identité", request.getTypePieceId()));

            try {
                nouvelEnum = TypePieceIdentite.valueOf(nouveauRef.getCode());
            } catch (IllegalArgumentException ignored) {
                // Code étendu non présent dans l'enum legacy
            }
        } else if (request.getTypePiece() != null) {
            nouvelEnum = request.getTypePiece();
            nouveauRef = typePieceRefRepository.findByCodeIgnoreCase(request.getTypePiece().name())
                .orElse(null);
        } else {
            throw new IllegalArgumentException(
                "Type de pièce d'identité obligatoire (typePieceId ou typePiece).");
        }

        // ─── 3. Vérification d'unicité en EXCLUANT l'ID courant ────────────────
        if (nouveauRef != null) {
            Optional<PieceIdentite> doublon = repository
                .findByTypePieceRef_IdAndNumero(nouveauRef.getId(), request.getNumero());
            if (doublon.isPresent() && !doublon.get().getId().equals(pieceId)) {
                throw new IllegalArgumentException(
                    "Une autre pièce d'identité avec ce numéro existe déjà pour ce type.");
            }
        } else if (nouvelEnum != null) {
            Optional<PieceIdentite> doublon = repository
                .findByTypePieceAndNumero(nouvelEnum, request.getNumero());
            if (doublon.isPresent() && !doublon.get().getId().equals(pieceId)) {
                throw new IllegalArgumentException(
                    "Une autre pièce d'identité avec ce numéro existe déjà pour ce type.");
            }
        }

        // ─── 4. Mise à jour des valeurs ────────────────────────────────────────
        entite.setTypePiece(nouvelEnum);
        entite.setTypePieceRef(nouveauRef);
        entite.setNumero(request.getNumero());

        PieceIdentite sauvegarde = repository.save(entite);

        // ─── 5. Audit ──────────────────────────────────────────────────────────
        auditService.enregistrer("MODIFICATION", "PieceIdentite", sauvegarde.getId(), avant,
            Map.of("typePiece",
                    nouveauRef != null ? nouveauRef.getCode() : nouvelEnum.name(),
                   "numero", request.getNumero()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public void supprimer(UUID pieceId) {
        PieceIdentite piece = repository.findById(pieceId)
                .orElseThrow(() -> new ResourceNotFoundException("Piece d'identite", pieceId));
        auditService.enregistrer("SUPPRESSION", "PieceIdentite", pieceId,
                Map.of("numero", piece.getNumero()), null);
        repository.delete(piece);
    }

    private PersonnePhysique trouverOuLever(UUID id) {
        return personnePhysiqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personne physique", id));
    }
}
