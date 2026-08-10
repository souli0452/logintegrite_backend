// common/security/CurrentUserProvider.java
package bf.gov.ascelc.logintegrite_backend.common.security;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;

public interface CurrentUserProvider {
    Utilisateur utilisateurCourant();
}
