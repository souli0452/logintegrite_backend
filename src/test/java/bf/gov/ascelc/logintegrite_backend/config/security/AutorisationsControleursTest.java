package bf.gov.ascelc.logintegrite_backend.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garde-fou d'architecture : aucun endpoint qui modifie des donnees, ni aucun export, ne doit
 * dependre uniquement de "utilisateur authentifie". Chacun doit porter un @PreAuthorize
 * (sur la methode ou sur la classe).
 */
class AutorisationsControleursTest {

    private static final String PACKAGE_RACINE = "bf.gov.ascelc.logintegrite_backend";

    @Test
    void chaqueEndpointDEcritureEtChaqueExportPortentUnPreAuthorize() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<String> nonProteges = new ArrayList<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(PACKAGE_RACINE)) {
            Class<?> controleur = Class.forName(bd.getBeanClassName());
            boolean classeProtegee = AnnotatedElementUtils.hasAnnotation(controleur, PreAuthorize.class);
            for (Method m : controleur.getDeclaredMethods()) {
                if (!estSensible(controleur, m)) continue;
                if (!classeProtegee && !AnnotatedElementUtils.hasAnnotation(m, PreAuthorize.class)) {
                    nonProteges.add(controleur.getSimpleName() + "#" + m.getName());
                }
            }
        }
        assertThat(nonProteges)
                .as("Endpoints d'ecriture ou d'export sans @PreAuthorize")
                .isEmpty();
    }

    private static boolean estSensible(Class<?> controleur, Method m) {
        boolean ecriture = m.isAnnotationPresent(PostMapping.class) || m.isAnnotationPresent(PutMapping.class)
                || m.isAnnotationPresent(PatchMapping.class) || m.isAnnotationPresent(DeleteMapping.class);
        boolean export = controleur.getSimpleName().contains("Rapport") && m.isAnnotationPresent(GetMapping.class);
        return ecriture || export;
    }
}
