package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.model.OperatorsDebtPosTypeOrg;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.migration.wf.mapper.DebtPositionTypeOrgOperatorMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DebtPositionTypeOrgOperatorMapperTest {

  private DebtPositionTypeOrgOperatorMapper mapper;

  @BeforeEach
    void setUp() {
        String dataCipherPsw = "PSW";
        mapper = new DebtPositionTypeOrgOperatorMapper(dataCipherPsw);
    }


  @Test
  void test() {
    // Given
    Long debtPositionTypeOrgId = 0L;
    Long organizationId = 1L;
    DebtPositionTypeOrgOperatorMigrationFileDTO migrationFileDTO = TestUtils.getPodamFactory().manufacturePojo(DebtPositionTypeOrgOperatorMigrationFileDTO.class);

    // When
    OperatorsDebtPosTypeOrg result = mapper.mapToOperators(migrationFileDTO,debtPositionTypeOrgId,organizationId);

    // Then
    TestUtils.checkNotNullFields(result, "operatorDebtPosTypeOrgId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    Assertions.assertEquals(organizationId, result.getOrganizationId());
    Assertions.assertEquals(migrationFileDTO.getDebtPositionTypeOrgCode(), result.getDebtPositionTypeOrgCode());
    Assertions.assertEquals(debtPositionTypeOrgId, result.getDebtPositionTypeOrgId());
  }
}
