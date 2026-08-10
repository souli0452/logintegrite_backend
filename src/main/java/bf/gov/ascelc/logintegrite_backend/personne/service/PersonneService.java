package bf.gov.ascelc.logintegrite_backend.personne.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneSearchCriteria;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneResumeResponse;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.PersonneDocumentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.List;
import java.util.Map;

public interface PersonneService {
    Page<PersonneResumeResponse> rechercher(PersonneSearchCriteria criteria, Pageable pageable);
    PersonneResumeResponse obtenir(UUID id);
    java.util.List<java.util.Map<String, Object>> historiqueStatutsJudiciaires(java.util.UUID personneId);
    List<PersonneDocumentResponse> listerDocuments(UUID personneId);
}
