package bf.gov.ascelc.logintegrite_backend.config.security;

import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.common.security.SynchroniseurRoles;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class SynchronisationUtilisateurFilterTest {

    private final CurrentUserProvider provider = mock(CurrentUserProvider.class);
    private final SynchroniseurRoles synchroniseur = mock(SynchroniseurRoles.class);
    private final FilterChain chaine = mock(FilterChain.class);
    private final SynchronisationUtilisateurFilter filtre = new SynchronisationUtilisateurFilter(provider, synchroniseur);

    @BeforeEach
    void vider() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void nettoyer() {
        SecurityContextHolder.clearContext();
    }

    private void authentifier(String... roles) {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject("kc-1")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private void appeler() throws Exception {
        filtre.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chaine);
    }

    @Test
    void provisionneUnUtilisateurNouveauPuisLaisseLaRequetePasser() throws Exception {
        authentifier("AGENT");
        when(synchroniseur.dejaSynchronise("kc-1")).thenReturn(false);

        appeler();

        verify(provider).utilisateurCourant();
        verify(chaine).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void neFaitRienPourUnUtilisateurDejaSynchronise() throws Exception {
        authentifier("AGENT");
        when(synchroniseur.dejaSynchronise("kc-1")).thenReturn(true);

        appeler();

        verify(provider, never()).utilisateurCourant();
    }

    @Test
    void neProvisionnePasUnJetonSansRoleMetier() throws Exception {
        authentifier("offline_access");

        appeler();

        verify(provider, never()).utilisateurCourant();
    }

    @Test
    void unEchecDeProvisionnementNeBloquePasLaRequete() throws Exception {
        authentifier("ADMIN");
        when(synchroniseur.dejaSynchronise("kc-1")).thenReturn(false);
        doThrow(new IllegalStateException("base indisponible")).when(provider).utilisateurCourant();

        appeler();

        verify(chaine).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
