package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.NotificationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.NotificationResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.NotificationMapper;
import bf.gov.ascelc.logintegrite_backend.repository.NotificationRepository;
import bf.gov.ascelc.logintegrite_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Override
    public NotificationResponse create(NotificationRequest request) {
        Notification entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String destinataireId) {
        return repository.findByDestinataireIdOrderByCreatedAtDesc(destinataireId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyUnreadNotifications(String destinataireId) {
        return repository.findByDestinataireIdAndLueFalseOrderByCreatedAtDesc(destinataireId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markAsRead(UUID id, String userId) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'id : " + id));

        // Contrôle de propriété ajouté ici (IDOR)
        verifierDestinataire(entity, userId);

        entity.setLue(true);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public void markAllAsRead(String destinataireId) {
        // Pas de contrôle supplémentaire nécessaire : la requête est déjà filtrée par destinataireId
        List<Notification> unread = repository.findByDestinataireIdAndLueFalseOrderByCreatedAtDesc(destinataireId);
        unread.forEach(notification -> notification.setLue(true));
        repository.saveAll(unread);
    }

    @Override
    public void delete(UUID id, String userId) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'id : " + id));

        // Contrôle de propriété ajouté ici (IDOR)
        verifierDestinataire(entity, userId);

        repository.delete(entity);
    }

    // Vérifie que la notification appartient bien à l'utilisateur courant
    private void verifierDestinataire(Notification entity, String userId) {
        if (entity.getDestinataireId() == null || !entity.getDestinataireId().equals(userId)) {
            throw new AccessDeniedException(
                    "La notification " + entity.getId() + " n'appartient pas à l'utilisateur courant.");
        }
    }
}
