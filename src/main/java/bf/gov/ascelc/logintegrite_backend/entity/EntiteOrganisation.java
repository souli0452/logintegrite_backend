package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "entite_organisation")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EntiteOrganisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 50)
    private String type;

    @Column(nullable = false)
    private Boolean actif = true;
}
