// common/entity/IdentifiableEntity.java (complet - INCHANGE par rapport a l'origine)
package bf.gov.ascelc.logintegrite_backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.UUID;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@MappedSuperclass
public abstract class IdentifiableEntity {

    @Id
    @ColumnDefault("core.uuid_generate_v7()")
    @Generated(event = EventType.INSERT)
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
}
