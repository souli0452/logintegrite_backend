package bf.gov.ascelc.logintegrite_backend.mapper;

import bf.gov.ascelc.logintegrite_backend.dto.request.NotificationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.NotificationResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification entity);

    Notification toEntity(NotificationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(NotificationRequest request, @MappingTarget Notification entity);
}