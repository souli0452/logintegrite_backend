package bf.gov.ascelc.logintegrite_backend.referentiel.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "type_piece_identite", schema = "referentiels")
@Getter
@Setter
public class TypePieceIdentiteRef extends IdentifiableEntity {

    /**
     * Code technique immuable — doit correspondre à une valeur de l'enum Java
     * TypePieceIdentite quand présente. Utilisé par le code métier pour la
     * cohérence des workflows.
     */
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    /**
     * Libellé lisible modifiable par les administrateurs.
     */
    @Column(name = "libelle", nullable = false, length = 150)
    private String libelle;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}
