package bf.gov.ascelc.logintegrite_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "keycloakAuditorAware") // ← modifié
@EnableScheduling
public class LogintegriteBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(
            LogintegriteBackendApplication.class, args);
    }
}
