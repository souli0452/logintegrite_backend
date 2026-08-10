package bf.gov.ascelc.logintegrite_backend.referentiel.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "nationalite", schema = "referentiels")
@Getter
@Setter
public class Nationalite extends IdentifiableEntity {

    @Column(name = "libelle", nullable = false, unique = true, length = 150)
    private String libelle;

    @Column(name = "code_iso", length = 3)
    private String codeIso;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;
}
