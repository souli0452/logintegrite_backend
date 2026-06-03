package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String destinataireId;

    @Column(length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private LocalDateTime dateEnvoi;

    @Builder.Default
    private Boolean lue = false;

    @PrePersist
    public void prePersist() {
        if (dateEnvoi == null)
            dateEnvoi = LocalDateTime.now();
    }
}
