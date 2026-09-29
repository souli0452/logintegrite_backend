package bf.gov.ascelc.logintegrite_backend.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Adresse IP de l'appelant de la requete HTTP courante, pour le journal d'audit.
 * On utilise l'adresse de la connexion (getRemoteAddr) et non X-Forwarded-For, qui est
 * falsifiable par le client : derriere un reverse proxy de confiance, activer
 * server.forward-headers-strategy=native pour que Tomcat la resolve correctement.
 */
public final class ClientIp {

    private ClientIp() { }

    public static Optional<String> courante() {
        RequestAttributes attributs = RequestContextHolder.getRequestAttributes();
        if (attributs instanceof ServletRequestAttributes servlet) {
            HttpServletRequest requete = servlet.getRequest();
            return Optional.ofNullable(requete.getRemoteAddr());
        }
        return Optional.empty();
    }
}
