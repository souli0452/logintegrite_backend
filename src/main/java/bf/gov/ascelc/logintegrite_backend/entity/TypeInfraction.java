package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_infraction")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TypeInfraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;
}
