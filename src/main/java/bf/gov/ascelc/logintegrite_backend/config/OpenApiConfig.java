// config/OpenApiConfig.java
package bf.gov.ascelc.logintegrite_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    public OpenAPI logIntegriteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Log Integrite - API")
                        .description("Plateforme de traçabilité des mis en cause dans les affaires " +
                                     "de malversations financières - ASCE-LC")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ASCE-LC")
                                .email("contact@asce-lc.bf")
                                .url("https://www.asce-lc.bf"))
                        .license(new License().name("Usage interne ASCE-LC")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Developpement local"),
                        new Server().url("https://api.logintegrite.asce-lc.bf").description("Production")))
                // Declare le schema de securite : Bearer JWT via Keycloak. Le bouton
                // "Authorize" de Swagger UI apparait grace a ca, et l'utilisateur peut
                // coller son token une fois pour toutes plutot que de le renvoyer sur
                // chaque endpoint. addSecurityItem l'applique par defaut a TOUS les
                // endpoints - coherent avec notre SecurityConfig qui exige un token
                // partout sauf /swagger-ui/**, /v3/api-docs/**, /actuator/health.
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu via Keycloak (grant_type=password " +
                                             "sur le realm 'logintegrite')")));
    }
}
