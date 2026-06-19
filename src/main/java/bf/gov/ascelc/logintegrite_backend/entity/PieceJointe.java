package bf.gov.ascelc.logintegrite_backend.entity;

import bf.gov.ascelc.logintegrite_backend.abstracts.AuditEntity;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "piece_jointe")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointe extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnoreProperties("piecesJointes")
    private FicheMiseEnCause fiche;

    @Column(nullable = false, length = 255)
    private String nomFichier;

    @Column(length = 50)
    private String typeFichier;

    private Long tailleOctets;

    @Column(nullable = false, length = 500)
    private String urlStockage;


}