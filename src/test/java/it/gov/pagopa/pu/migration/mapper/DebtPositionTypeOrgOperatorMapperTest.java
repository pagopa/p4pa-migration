package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DebtPositionTypeOrgOperatorMapperTest {

  @Test
  void test() {
    // Given
    Long debtPositionTypeOrgId = 0L;
    Long organizationId = 1L;
    byte[] cfOperatorHash = "cfOperatorHash".getBytes();
    DebtPositionTypeOrgOperatorMigrationFileDTO migrationFileDTO = TestUtils.getPodamFactory().manufacturePojo(DebtPositionTypeOrgOperatorMigrationFileDTO.class);

    // When
    DebtPositionTypeOrgOperators result = DebtPositionTypeOrgOperatorMapper.mapToOperators(migrationFileDTO,debtPositionTypeOrgId,organizationId, cfOperatorHash);

    // Then
    TestUtils.checkNotNullFields(result, "operatorDebtPosTypeOrgId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    Assertions.assertEquals(cfOperatorHash, result.getCfOperatorHash());
    Assertions.assertEquals(organizationId, result.getOrganizationId());
    Assertions.assertEquals(migrationFileDTO.getDebtPositionTypeOrgCode(), result.getDebtPositionTypeOrgCode());
    Assertions.assertEquals(debtPositionTypeOrgId, result.getDebtPositionTypeOrgId());
  }
}
