package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.TypeInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.entity.TypeInfraction;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.TypeInfractionMapper;
import bf.gov.ascelc.logintegrite_backend.repository.TypeInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.service.TypeInfractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeInfractionServiceImpl implements TypeInfractionService {

    private final TypeInfractionRepository repository;
    private final TypeInfractionMapper mapper;

    @Override
    public TypeInfractionResponse create(TypeInfractionRequest request) {
        TypeInfraction entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public TypeInfractionResponse update(UUID id, TypeInfractionRequest request) {
        TypeInfraction entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + id));
        mapper.updateEntityFromRequest(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TypeInfractionResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeInfractionResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + id);
        }
        repository.deleteById(id);
    }
}