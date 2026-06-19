package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Table(name = "sauvegardes")
@Data
@SuperBuilder // Basculé en SuperBuilder pour l'uniformisation globale
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Sauvegarde extends AuditEntity {

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