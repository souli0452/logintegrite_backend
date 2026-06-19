package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notification")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends AuditEntity {

    @Column(nullable = false, length = 100)
    private String destinataireId;

    @Column(length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Builder.Default
    private Boolean lue = false;

    // Note : 'dateEnvoi' est remplacé par 'createdAt' hérité.
}