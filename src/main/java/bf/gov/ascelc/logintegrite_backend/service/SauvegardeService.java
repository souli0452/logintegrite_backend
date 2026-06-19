package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.SauvegardeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.SauvegardeResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SauvegardeService {
    SauvegardeResponse registrarDebut(SauvegardeRequest request);
    SauvegardeResponse registrarFin(UUID id, String statut, LocalDateTime dateFin);
    List<SauvegardeResponse> getHistorique();
    SauvegardeResponse getById(UUID id);
}