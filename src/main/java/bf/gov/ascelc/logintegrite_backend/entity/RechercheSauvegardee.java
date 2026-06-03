package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recherche_sauvegardee")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RechercheSauvegardee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String utilisateurId;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String criteres;

    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null)
            dateCreation = LocalDateTime.now();
    }
}
