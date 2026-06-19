package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.dto.request.RegionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RegionResponse;
import bf.gov.ascelc.logintegrite_backend.service.RegionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService service;

    @PostMapping
    public ResponseEntity<RegionResponse> create(@Valid @RequestBody RegionRequest request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionResponse> update(@PathVariable UUID id, @Valid @RequestBody RegionRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}