package bf.gov.ascelc.logintegrite_backend.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class DataIntegrityHandlerTest {

    private final DataIntegrityHandler handler = new DataIntegrityHandler();

    private static DataIntegrityViolationException erreurSql(String messagePostgres) {
        return new DataIntegrityViolationException("could not execute statement", new RuntimeException(messagePostgres));
    }

    @Test
    void doublonDonne409AvecLeChampEtLaValeur() {
        ProblemDetail pd = handler.gerer(erreurSql("ERROR: duplicate key value violates unique constraint \"uk123\"\n  Detail: Key (libelle)=(Complice) already exists."));

        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getDetail()).contains("Complice").contains("libelle");
        assertThat(pd.getProperties()).containsEntry("message", pd.getDetail());
    }

    @Test
    void champObligatoireDonne400() {
        ProblemDetail pd = handler.gerer(erreurSql("null value in column \"numero_dossier\" of relation \"dossier\" violates not-null constraint"));

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getDetail()).contains("numero_dossier");
    }

    @Test
    void referenceInexistanteDonne400SansNommerLesTables() {
        ProblemDetail pd = handler.gerer(erreurSql("Key (source_signalement_id)=(abc) is not present in table \"source_signalement\"."));

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getDetail()).doesNotContain("source_signalement\"");
    }

    @Test
    void contrainteCheckNeRevelePasLeNomInterneDeLaContrainte() {
        ProblemDetail pd = handler.gerer(erreurSql("new row for relation \"peine\" violates check constraint \"chk_secret_interne\""));

        assertThat(pd.getStatus()).isEqualTo(400);
        assertThat(pd.getDetail()).doesNotContain("chk_secret_interne").doesNotContain("peine");
    }

    @Test
    void erreurInconnueNeRecopiePasLeTexteSqlEtDonneUneReference() {
        ProblemDetail pd = handler.gerer(erreurSql("ERROR: exclusion constraint \"ex_dossiers_schema_prive\" violated on table dossiers.dossier"));

        assertThat(pd.getStatus()).isEqualTo(409);
        assertThat(pd.getDetail()).doesNotContain("ex_dossiers_schema_prive").doesNotContain("dossiers.dossier");
        assertThat(pd.getProperties()).containsKey("reference");
    }
}
