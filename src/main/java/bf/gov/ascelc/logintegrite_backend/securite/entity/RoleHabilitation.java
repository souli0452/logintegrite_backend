// securite/entity/RoleHabilitation.java
package bf.gov.ascelc.logintegrite_backend.securite.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "role_habilitation", schema = "securite")
@Getter
@Setter
public class RoleHabilitation extends IdentifiableEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "code", columnDefinition = "core.code_role", nullable = false, unique = true)
    private CodeRole code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "acces_vue_globale_dossier", nullable = false)
    private boolean accesVueGlobaleDossier = false;
}
