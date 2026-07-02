package bf.gov.ascelc.logintegrite_backend.config.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JwtRoleUtils {

    private JwtRoleUtils() {
    }

    @SuppressWarnings("unchecked")
    public static List<String> extraireRoles(Jwt jwt) {
        if (jwt == null || !jwt.hasClaim("realm_access")) {
            return Collections.emptyList();
        }
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }
        return ((List<?>) realmAccess.get("roles")).stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public static boolean estRolePublicUniquement(Jwt jwt) {
        List<String> roles = extraireRoles(jwt);
        return roles.contains("public")
                && !roles.contains("ADMINISTRATEUR")
                && !roles.contains("AGENT")
                && !roles.contains("VALIDATEUR");
    }


    public static boolean estAdministrateur(Jwt jwt) {
        return extraireRoles(jwt).contains("ADMINISTRATEUR");
    }
}