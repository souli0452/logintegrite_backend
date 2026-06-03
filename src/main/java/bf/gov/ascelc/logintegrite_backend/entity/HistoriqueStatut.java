package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause.StatutJudiciaire;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_statut")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnoreProperties("historiqueStatuts") // Évite la boucle sur l'historique
    private FicheMiseEnCause fiche;

    @Enumerated(EnumType.STRING)
    private StatutJudiciaire ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutJudiciaire nouveauStatut;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(nullable = false)
    private LocalDateTime dateChangement;

    private LocalDate dateJugement;
    private String juridiction;
    private String typePeine;
    private String dureePeine;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantAmende;

    @Column(columnDefinition = "TEXT")
    private String motifRelaxe;

    @Column(length = 100)
    private String agentId;

    @PrePersist
    public void prePersist() {
        if (dateChangement == null)
            dateChangement = LocalDateTime.now();
    }
}
