package bf.gov.ascelc.logintegrite_backend.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak.admin")
@Getter
@Setter
public class KeycloakAdminProperties {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}