package bf.gov.ascelc.logintegrite_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "infraction")
@Getter
@Setter
@SuperBuilder // Changement important : SuperBuilder est obligatoire pour l'héritage avec Lombok
@NoArgsConstructor
@AllArgsConstructor
public class Infraction extends AuditEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnoreProperties("infractions")
    private FicheMiseEnCause fiche;

    @Column(name = "nature", nullable = false, length = 30)
    private String nature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_infraction_id")
    private TypeInfraction typeInfraction;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "date_faits", nullable = false)
    private LocalDate dateFaits;

    @Column(name = "lieu_faits", length = 200)
    private String lieuFaits;

    @Column(precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(length = 10)
    @Builder.Default
    private String devise = "XOF";

    @Column(columnDefinition = "TEXT")
    private String sources;
}