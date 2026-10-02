package bf.gov.ascelc.logintegrite_backend.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AccesConsultantFilterTest {

    private static UsernamePasswordAuthenticationToken avec(String... roles) {
        return UsernamePasswordAuthenticationToken.authenticated("u", "n/a",
                Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList());
    }

    @Test
    void la_liste_blanche_ne_contient_que_la_verification_et_le_signalement_du_poste() {
        assertTrue(AccesConsultantFilter.autorise("GET", "/api/v1/verification/recherche"));
        assertTrue(AccesConsultantFilter.autorise("GET", "/api/v1/verification/personnes/abc"));
        assertTrue(AccesConsultantFilter.autorise("POST", "/api/v1/verification/personnes/abc/demande-export"));
        assertTrue(AccesConsultantFilter.autorise("POST", "/api/v1/audit/evenements-poste"));

        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/personnes/en-instruction"));
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/personnes/recherche"));
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/personnes/registre-officiel"));
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/dossiers"));
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/audit/evenements-poste"));
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/demandes-export"));
        assertFalse(AccesConsultantFilter.autorise("PUT", "/api/v1/demandes-export/x/decision"));
        // pas de contournement par un prefixe qui ressemble
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/verificationX"));
        assertFalse(AccesConsultantFilter.autorise("GET", "/api/v1/../v1/dossiers"));
    }

    @Test
    void seul_un_compte_de_consultation_pure_est_concerne() {
        assertTrue(AccesConsultantFilter.estConsultantSeul(avec("ROLE_CONSULTANT")));
        assertFalse(AccesConsultantFilter.estConsultantSeul(avec("ROLE_CONSULTANT", "ROLE_AGENT")));
        assertFalse(AccesConsultantFilter.estConsultantSeul(avec("ROLE_ADMIN")));
        assertFalse(AccesConsultantFilter.estConsultantSeul(avec("ROLE_VALIDATEUR", "ROLE_CONSULTANT")));
        assertFalse(AccesConsultantFilter.estConsultantSeul(null));
    }

    @Test
    void un_consultant_est_refuse_en_403_hors_liste_blanche_et_la_requete_n_avance_pas() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(avec("ROLE_CONSULTANT"));
        try {
            MockHttpServletRequest requete = new MockHttpServletRequest("GET", "/api/v1/dossiers");
            requete.setRequestURI("/api/v1/dossiers");
            MockHttpServletResponse reponse = new MockHttpServletResponse();
            FilterChain chaine = mock(FilterChain.class);

            new AccesConsultantFilter().doFilter(requete, reponse, chaine);

            assertEquals(403, reponse.getStatus());
            verify(chaine, never()).doFilter(requete, reponse);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void un_agent_n_est_pas_filtre() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(avec("ROLE_AGENT"));
        try {
            MockHttpServletRequest requete = new MockHttpServletRequest("GET", "/api/v1/dossiers");
            requete.setRequestURI("/api/v1/dossiers");
            MockHttpServletResponse reponse = new MockHttpServletResponse();
            FilterChain chaine = mock(FilterChain.class);

            new AccesConsultantFilter().doFilter(requete, reponse, chaine);

            assertEquals(200, reponse.getStatus());
            verify(chaine).doFilter(requete, reponse);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
