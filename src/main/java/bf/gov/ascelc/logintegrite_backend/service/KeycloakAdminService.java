package bf.gov.ascelc.logintegrite_backend.service;

import java.util.List;

public interface KeycloakAdminService {
    List<String> listerIdsUtilisateursParRole(String role);
}