package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntity;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "historique_statut")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueStatut extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnoreProperties("historiqueStatuts")
    private FicheMiseEnCause fiche;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infraction_id")
    @JsonIgnoreProperties({"fiche"})
    private Infraction infraction;

    @Column(name = "ancien_statut", length = 50)
    private String ancientStatut;

    @Column(name = "nouveau_statut", nullable = false, length = 50)
    private String nouveauStatut;

    @Column(columnDefinition = "TEXT")
    private String motif;

    private LocalDate dateJugement;

    @Column(length = 200)
    private String juridiction;

    @Column(length = 200)
    private String typePeine;

    @Column(length = 100)
    private String dureePeine;

    @Column(precision = 15, scale = 2)
    private BigDecimal montantAmende;

    @Column(columnDefinition = "TEXT")
    private String motifRelaxe;
}