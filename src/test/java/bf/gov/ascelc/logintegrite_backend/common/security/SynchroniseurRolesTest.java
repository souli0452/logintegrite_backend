package bf.gov.ascelc.logintegrite_backend.common.security;

import bf.gov.ascelc.logintegrite_backend.securite.entity.RoleHabilitation;
import bf.gov.ascelc.logintegrite_backend.securite.entity.Utilisateur;
import bf.gov.ascelc.logintegrite_backend.securite.entity.UtilisateurRole;
import bf.gov.ascelc.logintegrite_backend.securite.enums.CodeRole;
import bf.gov.ascelc.logintegrite_backend.securite.repository.RoleHabilitationRepository;
import bf.gov.ascelc.logintegrite_backend.securite.repository.UtilisateurRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SynchroniseurRolesTest {

    private final UtilisateurRoleRepository liens = mock(UtilisateurRoleRepository.class);
    private final RoleHabilitationRepository roles = mock(RoleHabilitationRepository.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private SynchroniseurRoles synchroniseur;
    private Utilisateur utilisateur;
    private RoleHabilitation roleAdmin;

    @BeforeEach
    void init() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        synchroniseur = new SynchroniseurRoles(liens, roles, transactionManager);

        utilisateur = new Utilisateur();
        utilisateur.setKeycloakId("kc-1");
        ReflectionTestUtils.setField(utilisateur, "id", UUID.randomUUID());

        roleAdmin = new RoleHabilitation();
        ReflectionTestUtils.setField(roleAdmin, "id", UUID.randomUUID());
        roleAdmin.setCode(CodeRole.ADMIN);
        when(roles.findByCode(CodeRole.ADMIN)).thenReturn(Optional.of(roleAdmin));
    }

    private static Jwt jetonAvecRoles(String... noms) {
        return Jwt.withTokenValue("t").header("alg", "none").subject("kc-1")
                .claim("realm_access", Map.of("roles", List.of(noms)))
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    @Test
    void ajouteLeRoleMetierAbsentDeLaTableLocale() {
        when(liens.existsByUtilisateur_IdAndRoleHabilitation_Id(utilisateur.getId(), roleAdmin.getId())).thenReturn(false);

        synchroniseur.synchroniser(utilisateur, jetonAvecRoles("admin", "offline_access"));

        verify(liens).save(any(UtilisateurRole.class));
        verify(roles, never()).findByCode(CodeRole.AGENT);   // "offline_access" n'est pas un role metier
    }

    @Test
    void neCreePasDeDoublon() {
        when(liens.existsByUtilisateur_IdAndRoleHabilitation_Id(utilisateur.getId(), roleAdmin.getId())).thenReturn(true);

        synchroniseur.synchroniser(utilisateur, jetonAvecRoles("ADMIN"));

        verify(liens, never()).save(any());
    }

    @Test
    void neRefaitPasLeTravailPourUnUtilisateurDejaSynchronise() {
        when(liens.existsByUtilisateur_IdAndRoleHabilitation_Id(any(), any())).thenReturn(false);

        synchroniseur.synchroniser(utilisateur, jetonAvecRoles("ADMIN"));
        synchroniseur.synchroniser(utilisateur, jetonAvecRoles("ADMIN"));

        verify(liens, times(1)).save(any(UtilisateurRole.class));
    }

    @Test
    void uneEchecNeFaitJamaisEchouerLaRequeteEtSeraReessaye() {
        when(liens.existsByUtilisateur_IdAndRoleHabilitation_Id(any(), any())).thenReturn(false);
        when(liens.save(any())).thenThrow(new IllegalStateException("base indisponible")).thenAnswer(i -> i.getArgument(0));

        synchroniseur.synchroniser(utilisateur, jetonAvecRoles("ADMIN"));   // ne leve rien
        synchroniseur.synchroniser(utilisateur, jetonAvecRoles("ADMIN"));   // reessaye, cette fois avec succes

        verify(liens, times(2)).save(any(UtilisateurRole.class));
    }

    @Test
    void deuxRequetesSimultanees_neCreentQuUnSeulLien() throws Exception {
        // existsBy renvoie toujours false : sans verrou, chaque thread inserait le lien.
        when(liens.existsByUtilisateur_IdAndRoleHabilitation_Id(any(), any())).thenReturn(false);
        when(liens.save(any())).thenAnswer(i -> {
            Thread.sleep(150);   // laisse le second thread arriver pendant l'ecriture du premier
            return i.getArgument(0);
        });

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch depart = new CountDownLatch(1);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                depart.await();
                synchroniseur.synchroniser(utilisateur, jetonAvecRoles("ADMIN"));
                return null;
            });
        }
        depart.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        verify(liens, times(1)).save(any(UtilisateurRole.class));
    }

    @Test
    void ignoreUnJetonSansRolesEtUnUtilisateurNonEnregistre() {
        synchroniseur.synchroniser(utilisateur, Jwt.withTokenValue("t").header("alg", "none").subject("kc-1")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build());
        synchroniseur.synchroniser(new Utilisateur(), jetonAvecRoles("ADMIN"));

        verify(liens, never()).save(any());
    }
}
