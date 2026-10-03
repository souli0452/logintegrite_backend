// document/service/impl/DocumentServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.document.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.common.storage.FichierValidator;
import bf.gov.ascelc.logintegrite_backend.common.storage.StorageService;
import bf.gov.ascelc.logintegrite_backend.document.dto.request.DocumentRequest;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import bf.gov.ascelc.logintegrite_backend.document.entity.Document;
import bf.gov.ascelc.logintegrite_backend.document.entity.DocumentImplication;
import bf.gov.ascelc.logintegrite_backend.document.mapper.DocumentMapper;
import bf.gov.ascelc.logintegrite_backend.document.repository.DocumentImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.document.repository.DocumentRepository;
import bf.gov.ascelc.logintegrite_backend.document.service.DocumentService;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repository;
    private final DocumentImplicationRepository documentImplicationRepository;
    private final DossierRepository dossierRepository;
    private final ImplicationRepository implicationRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final StorageService storageService;
    private final FichierValidator fichierValidator;
    private final CurrentUserProvider currentUserProvider;
    private final DocumentMapper mapper;
    private final AuditService auditService;
    private final ConsultationService consultationService;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listerParDossier(UUID dossierId) {
        return repository.findByDossierId(dossierId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public DocumentResponse deposer(UUID dossierId, DocumentRequest request) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier", dossierId));

        bf.gov.ascelc.logintegrite_backend.dossier.service.RegleDossier.verifierOuvert(dossier);

        MultipartFile fichier = request.getFichier();
        FichierValidator.FichierValide valide = fichierValidator.valider(fichier);
        String nomStockage = UUID.randomUUID() + "." + valide.extension();

        String hash;
        String cheminStockage;
        try {
            hash = calculerHashSha256(fichier);
            cheminStockage = storageService.stocker(fichier, nomStockage);
        } catch (IOException e) {
            throw new UncheckedIOException("Echec du stockage du fichier", e);
        }

        Document document = new Document();
        document.setDossier(dossier);
        document.setTypeDocument(typeDocumentRepository.findById(request.getTypeDocumentId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de document", request.getTypeDocumentId())));
        document.setNomOriginal(valide.nomOriginal());
        document.setNomStockage(nomStockage);
        document.setCheminStockage(cheminStockage);
        document.setTypeMime(valide.typeMime());
        document.setTailleOctets(fichier.getSize());
        document.setHashIntegrite(hash);
        document.setImmuable(true);
        document.setUploadePar(currentUserProvider.utilisateurCourant());
        Document sauvegarde = repository.save(document);

        auditService.enregistrer("DEPOT", "Document", sauvegarde.getId(), null,
                Map.of("nomOriginal", sauvegarde.getNomOriginal(),
                       "hashIntegrite", sauvegarde.getHashIntegrite(),
                       "dossierId", dossier.getId().toString()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse obtenir(UUID documentId) {
        Document entite = trouverOuLever(documentId);
        consultationService.enregistrer("Document", documentId);
        return mapper.toResponse(entite);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource telechargerFichier(UUID documentId) {
        Document entite = trouverOuLever(documentId);
        consultationService.enregistrer("Document.telechargement", documentId);
        return storageService.recuperer(entite.getCheminStockage());
    }

    @Override
    public void tagger(UUID documentId, UUID implicationId) {
        if (documentImplicationRepository.existsByDocument_IdAndImplication_Id(documentId, implicationId)) {
            return; // deja tague, idempotent
        }
        Document document = trouverOuLever(documentId);
        Implication implication = implicationRepository.findById(implicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Implication", implicationId));

        DocumentImplication lien = new DocumentImplication();
        lien.setDocument(document);
        lien.setImplication(implication);
        documentImplicationRepository.save(lien);

        auditService.enregistrer("TAG_DOCUMENT", "Document", documentId, null,
                Map.of("implicationId", implicationId.toString()));
    }

    @Override
    public void retirerTag(UUID documentId, UUID implicationId) {
        documentImplicationRepository.deleteByDocument_IdAndImplication_Id(documentId, implicationId);
        auditService.enregistrer("RETRAIT_TAG_DOCUMENT", "Document", documentId,
                Map.of("implicationId", implicationId.toString()), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listerVisiblesPourImplication(UUID implicationId) {
        Implication implication = implicationRepository.findById(implicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Implication", implicationId));
        return repository.findVisiblesPourImplication(implication.getDossier().getId(), implicationId)
                .stream().map(mapper::toResponse).toList();
    }

    private Document trouverOuLever(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }

    /** SHA-256 calcule en flux (pas de chargement du fichier entier en memoire). */
    private String calculerHashSha256(MultipartFile fichier) throws IOException {
        try (InputStream in = fichier.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] tampon = new byte[8192];
            int lus;
            while ((lus = in.read(tampon)) != -1) {
                digest.update(tampon, 0, lus);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
