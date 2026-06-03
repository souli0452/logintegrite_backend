package bf.gov.ascelc.logintegrite_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "infraction")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class Infraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnoreProperties("infractions") // Coupe la boucle infinie de sérialisation JSON globale
    private FicheMiseEnCause fiche;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NatureInfraction nature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_infraction_id")
    private TypeInfraction typeInfraction;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateFaits;

    @Column(length = 200)
    private String lieuFaits;

    @Column(precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(length = 10)
    @Builder.Default // CORRECTION WARNING : Empêche Lombok de vider la valeur par défaut "XOF" lors du build
    private String devise = "XOF";

    @Column(columnDefinition = "TEXT")
    private String sources;

    public enum NatureInfraction {
        CORRUPTION, DETOURNEMENT, FRAUDE,
        CONCUSSION, PRISE_ILLEGALE, FAVORITISME, AUTRE
    }
}
