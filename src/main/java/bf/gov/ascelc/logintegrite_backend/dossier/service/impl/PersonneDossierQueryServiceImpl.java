package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResumeResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PersonneDossierCompletResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.FaitReproche;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.DossierMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.FaitReprocheMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.ImplicationMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.FaitReprocheRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationFaitRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.PersonneDossierQueryService;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import bf.gov.ascelc.logintegrite_backend.document.mapper.DocumentMapper;
import bf.gov.ascelc.logintegrite_backend.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonneDossierQueryServiceImpl implements PersonneDossierQueryService {

    private final ImplicationRepository implicationRepository;
    private final FaitReprocheRepository faitReprocheRepository;
    private final DocumentRepository documentRepository;
    private final ImplicationMapper implicationMapper;
    private final DossierMapper dossierMapper;
    private final FaitReprocheMapper faitReprocheMapper;
    private final DocumentMapper documentMapper;
    private final ImplicationFaitRepository implicationFaitRepository;

    @Override
    public PersonneDossierCompletResponse obtenirPourPersonne(UUID personneId) {
        List<Implication> implications = implicationRepository.findByPersonneId(personneId);

        List<Dossier> dossiers = implications.stream()
                .map(Implication::getDossier)
                .distinct()
                .toList();

        List<UUID> dossierIds = dossiers.stream().map(Dossier::getId).toList();

        List<FaitReproche> faits = dossierIds.isEmpty()
                ? List.of()
                : faitReprocheRepository.findByDossierIdIn(dossierIds);

        List<DocumentResponse> documents = dossierIds.isEmpty()
                ? List.of()
                : documentRepository.findByDossierIdIn(dossierIds).stream()
                    .map(documentMapper::toResponse)
                    .toList();

        return PersonneDossierCompletResponse.builder()
                .implications(implications.stream().map(implicationMapper::toResponse).toList())
                .dossiers(dossiers.stream().map(dossierMapper::toResponse).toList())
                .faits(faits.stream().map(faitReprocheMapper::toResponse).toList())
                .documents(documents)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ImplicationFaitResumeResponse> listerImplicationFaitsPourPersonne(UUID personneId) {
        List<ImplicationFait> liaisons = implicationFaitRepository.findAllByImplication_PersonneId(personneId);
        return liaisons.stream()
                .map(this::versResumeResponse)
                .toList();
    }

    private ImplicationFaitResumeResponse versResumeResponse(ImplicationFait lien) {
        var fait = lien.getFaitReproche();
        var implication = lien.getImplication();
        var dossier = fait.getDossier();

        return ImplicationFaitResumeResponse.builder()
                .id(lien.getId())
                .implicationId(implication.getId())
                .faitReprocheId(fait.getId())
                .dossierId(dossier.getId())
                .personneId(implication.getPersonne().getId())
                .numeroDossier(dossier.getNumeroDossier())
                .intituleDossier(dossier.getIntitule())
                .typeInfractionLibelle(fait.getTypeInfraction() != null
                        ? fait.getTypeInfraction().getLibelle() : null)
                .faitDescription(fait.getDescription())
                .faitDateFaits(fait.getDateFaits() != null ? fait.getDateFaits().toString() : null)
                .statutValidation(fait.getStatutValidation() != null
                        ? fait.getStatutValidation().name() : null)
                .build();
    }
}
