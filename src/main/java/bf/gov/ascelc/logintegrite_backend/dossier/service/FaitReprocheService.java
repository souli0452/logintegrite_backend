package bf.gov.ascelc.logintegrite_backend.dossier.service;


import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.FaitReprocheRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.RejetFaitRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitRejeteResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.FaitReprocheResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutValidation;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierAValiderResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FaitReprocheService {
    List<FaitReprocheResponse> listerParDossier(UUID dossierId);
    FaitReprocheResponse creer(UUID dossierId, FaitReprocheRequest request);
    FaitReprocheResponse valider(UUID faitId);
    FaitReprocheResponse rejeter(UUID faitId, RejetFaitRequest request);
    Page<FaitReprocheResponse> listerParStatut(StatutValidation statut, Pageable pageable);
    List<DossierAValiderResponse> listerDossiersAvecFaitsEnAttente();
    List<FaitRejeteResponse> listerRejetes();
    FaitReprocheResponse reprendre(UUID faitId);
}
