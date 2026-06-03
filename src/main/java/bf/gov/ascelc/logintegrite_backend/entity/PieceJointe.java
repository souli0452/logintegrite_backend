package bf.gov.ascelc.logintegrite_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "piece_jointe")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnoreProperties("piecesJointes") // Évite la boucle sur les pièces jointes
    private FicheMiseEnCause fiche;

    @Column(nullable = false, length = 255)
    private String nomFichier;

    @Column(length = 50)
    private String typeFichier;

    private Long tailleOctets;

    @Column(nullable = false, length = 500)
    private String urlStockage;

    private LocalDateTime dateUpload;

    @Column(length = 100)
    private String uploadPar;

    @PrePersist
    public void prePersist() {
        if (dateUpload == null)
            dateUpload = LocalDateTime.now();
    }
}
