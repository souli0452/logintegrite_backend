package bf.gov.ascelc.logintegrite_backend.common.security;

import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CurrentUserProviderKeycloakTest {

    private final UtilisateurRepository repository = mock(UtilisateurRepository.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private final SynchroniseurRoles synchroniseurRoles = mock(SynchroniseurRoles.class);
    private CurrentUserProviderKeycloak provider;

    @BeforeEach
    void init() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        provider = new CurrentUserProviderKeycloak(repository, transactionManager, synchroniseurRoles);
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject("kc-42")
                .claim("given_name", "Awa").claim("family_name", "Ouedraogo").claim("email", "awa@asce-lc.bf")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void nettoyer() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void retourneUtilisateurExistantSansOuvrirDeTransaction() {
        Utilisateur existant = new Utilisateur();
        when(repository.findByKeycloakId("kc-42")).thenReturn(Optional.of(existant));

        assertThat(provider.utilisateurCourant()).isSameAs(existant);
        verifyNoInteractions(transactionManager);
        verify(synchroniseurRoles).synchroniser(any(Utilisateur.class), any(Jwt.class));
        verify(repository, never()).save(any());
    }

    @Test
    void provisionneDansUneTransactionRequiresNewIndependante() {
        when(repository.findByKeycloakId("kc-42")).thenReturn(Optional.empty());
        when(repository.save(any(Utilisateur.class))).thenAnswer(i -> i.getArgument(0));

        Utilisateur cree = provider.utilisateurCourant();

        assertThat(cree.getKeycloakId()).isEqualTo("kc-42");
        assertThat(cree.getPrenom()).isEqualTo("Awa");
        assertThat(cree.getEmail()).isEqualTo("awa@asce-lc.bf");
        // Preuve que la transaction independante est reellement ouverte (une annotation @Transactional
        // sur une methode appelee en interne serait, elle, sans effet).
        verify(transactionManager).getTransaction(argThat(def ->
                def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        verify(transactionManager).commit(any());
    }

    @Test
    void relitUtilisateurSiUneRequeteConcurrenteLAdejaCree() {
        Utilisateur concurrent = new Utilisateur();
        when(repository.findByKeycloakId("kc-42")).thenReturn(Optional.empty(), Optional.of(concurrent));
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("uk_keycloak_id"));

        assertThat(provider.utilisateurCourant()).isSameAs(concurrent);
    }
}
