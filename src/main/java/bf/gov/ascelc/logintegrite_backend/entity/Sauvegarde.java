package bf.gov.ascelc.logintegrite_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sauvegardes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sauvegarde {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(nullable = false)
    private String type; // Reçoit AUTO ou MANUEL sans classe externe

    @Column(nullable = false)
    private String statut;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;
}
