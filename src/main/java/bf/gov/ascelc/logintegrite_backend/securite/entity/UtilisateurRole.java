// securite/entity/UtilisateurRole.java
package bf.gov.ascelc.logintegrite_backend.securite.entity;

import bf.gov.ascelc.logintegrite_backend.common.entity.IdentifiableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "utilisateur_role", schema = "securite")
@Getter
@Setter
public class UtilisateurRole extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, updatable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_habilitation_id", nullable = false, updatable = false)
    private RoleHabilitation roleHabilitation;

    @Column(name = "date_attribution", insertable = false, updatable = false)
    private Instant dateAttribution;
}
