package bf.gov.ascelc.logintegrite_backend.dossier;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Implication;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.ImplicationMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.impl.ImplicationServiceImpl;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.RoleImplication;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.RoleImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.StatutJudiciaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetraitImplicationTest {

    private final UUID dossierId = UUID.randomUUID();
    private final UUID implicationId = UUID.randomUUID();
    private ImplicationRepository repository;
    private JdbcTemplate jdbc;
    private ImplicationServiceImpl service;
    private Implication implication;
    private Dossier dossier;

    @BeforeEach
    void preparer() {
        repository = mock(ImplicationRepository.class);
        jdbc = mock(JdbcTemplate.class);
        service = new ImplicationServiceImpl(repository, mock(DossierRepository.class), mock(PersonneRepository.class),
                mock(RoleImplicationRepository.class), mock(EntiteOrganisationRepository.class),
                mock(StatutJudiciaireRepository.class), mock(ImplicationMapper.class), mock(AuditService.class), jdbc);

        dossier = mock(Dossier.class);
        when(dossier.getId()).thenReturn(dossierId);
        when(dossier.getStatutDossier()).thenReturn(StatutDossier.OUVERT);
        Personne personne = mock(Personne.class);
        when(personne.getId()).thenReturn(UUID.randomUUID());
        RoleImplication role = mock(RoleImplication.class);
        when(role.getLibelle()).thenReturn("Auteur principal");
        implication = mock(Implication.class);
        when(implication.getDossier()).thenReturn(dossier);
        when(implication.getPersonne()).thenReturn(personne);
        when(implication.getRoleImplication()).thenReturn(role);
        when(repository.findById(implicationId)).thenReturn(Optional.of(implication));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(implicationId))).thenReturn(0);
    }

    @Test
    void retire_une_personne_sans_fait_valide_ni_peine() {
        service.supprimer(dossierId, implicationId);
        verify(repository).delete(implication);
        verify(jdbc).update("delete from documents.document_implication where implication_id = ?", implicationId);
        verify(jdbc).update("delete from dossiers.implication_fait where implication_id = ?", implicationId);
    }

    @Test
    void refuse_si_un_fait_valide_la_concerne() {
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("statut_validation = 'VALIDEE'"),
                eq(Integer.class), eq(implicationId))).thenReturn(1);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.supprimer(dossierId, implicationId));
        assertEquals(409, e.getStatusCode().value());
        verify(repository, never()).delete(implication);
    }

    @Test
    void refuse_si_une_peine_est_enregistree() {
        when(jdbc.queryForObject(org.mockito.ArgumentMatchers.contains("dossiers.peine"),
                eq(Integer.class), eq(implicationId))).thenReturn(1);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.supprimer(dossierId, implicationId));
        assertEquals(409, e.getStatusCode().value());
        verify(repository, never()).delete(implication);
    }

    @Test
    void refuse_dans_un_dossier_cloture() {
        when(dossier.getStatutDossier()).thenReturn(StatutDossier.CLOTURE);
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> service.supprimer(dossierId, implicationId));
        assertEquals(409, e.getStatusCode().value());
        verify(repository, never()).delete(implication);
    }

    @Test
    void une_implication_d_un_autre_dossier_est_introuvable() {
        assertThrows(ResourceNotFoundException.class, () -> service.supprimer(UUID.randomUUID(), implicationId));
        verify(repository, never()).delete(implication);
    }
}
