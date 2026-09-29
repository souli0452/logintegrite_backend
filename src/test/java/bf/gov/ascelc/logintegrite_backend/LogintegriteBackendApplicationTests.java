package bf.gov.ascelc.logintegrite_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'integration sur un vrai PostgreSQL : Flyway applique les migrations, puis Hibernate
 * valide le mapping des entites contre le schema (ddl-auto=validate). Ignore si Docker est absent.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "keycloak.admin.client-secret=test-secret",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:0/certs"
})
class LogintegriteBackendApplicationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void contextLoadsEtMigrationsAppliquees() {
        Integer referentiels = jdbc.queryForObject("SELECT count(*) FROM referentiels.type_infraction", Integer.class);
        assertThat(referentiels).isPositive();
    }

    @Test
    void chaineDAuditEstChaineeEtImmuable() {
        jdbc.update("INSERT INTO audit.journal_audit (id, action, entite_cible, date_action) VALUES (gen_random_uuid(), 'CREATION', 'Test', now())");
        jdbc.update("INSERT INTO audit.journal_audit (id, action, entite_cible, date_action) VALUES (gen_random_uuid(), 'MODIFICATION', 'Test', now())");

        Integer ruptures = jdbc.queryForObject("SELECT count(*) FROM audit.verifier_integrite_chaine(NULL)", Integer.class);
        assertThat(ruptures).isZero();

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> jdbc.update("UPDATE audit.journal_audit SET action = 'X'"));
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> jdbc.update("DELETE FROM audit.journal_audit"));
    }
}
