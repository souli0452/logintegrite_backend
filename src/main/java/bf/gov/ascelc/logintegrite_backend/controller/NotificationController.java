package bf.gov.ascelc.logintegrite_backend.controller;

import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import bf.gov.ascelc.logintegrite_backend.repository.NotificationRepository;
import bf.gov.ascelc.logintegrite_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static bf.gov.ascelc.logintegrite_backend.utils.constants.ApiURLs.*;

@RestController
@RequestMapping(NOTIFICATIONS) // "/api/notifications"
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notifRepo;
    private final NotificationService notifService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Notification>> mesNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";

        return ResponseEntity.ok(
                notifRepo.findByDestinataireIdOrderByDateEnvoiDesc(
                        userId,
                        PageRequest.of(page, size)));
    }

    @GetMapping(NOTIFICATIONS_COUNT) // "/count-non-lues"
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countNonLues(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        return ResponseEntity.ok(notifService.countNonLues(userId));
    }

    @PutMapping(NOTIFICATIONS_MARQUER_LUES) // "/marquer-lues"
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marquerLues(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt != null ? jwt.getSubject() : "SYSTEM";
        notifService.marquerToutesLues(userId);
        return ResponseEntity.ok().build();
    }
}