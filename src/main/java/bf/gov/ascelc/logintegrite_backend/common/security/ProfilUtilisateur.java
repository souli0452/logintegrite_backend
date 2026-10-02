package bf.gov.ascelc.logintegrite_backend.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

/** Question de profil posee a l'utilisateur de la requete courante. */
public final class ProfilUtilisateur {

    private static final Set<String> ROLES_NON_CONSULTANT = Set.of("ROLE_ADMIN", "ROLE_AGENT", "ROLE_VALIDATEUR");

    private ProfilUtilisateur() { }

    /** Vrai si le compte est un compte de consultation SEULE (aucun role agent, validateur ou administrateur). */
    public static boolean estConsultantSeul(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        boolean consultant = false;
        for (GrantedAuthority a : authentication.getAuthorities()) {
            String role = a.getAuthority();
            if (ROLES_NON_CONSULTANT.contains(role)) return false;
            if ("ROLE_CONSULTANT".equals(role)) consultant = true;
        }
        return consultant;
    }

    public static boolean estConsultantSeul() {
        return estConsultantSeul(SecurityContextHolder.getContext().getAuthentication());
    }
}
