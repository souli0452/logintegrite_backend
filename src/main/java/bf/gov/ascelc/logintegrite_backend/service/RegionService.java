package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.RegionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RegionResponse;
import java.util.List;
import java.util.UUID;

public interface RegionService {
    RegionResponse create(RegionRequest request);
    RegionResponse update(UUID id, RegionRequest request);
    RegionResponse getById(UUID id);
    List<RegionResponse> getAll();
    void delete(UUID id);
}