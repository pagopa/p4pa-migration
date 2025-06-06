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
    DebtPositionTypeOrgOperatorMigrationFileDTO ingestionFlowFile = TestUtils.getPodamFactory().manufacturePojo(DebtPositionTypeOrgOperatorMigrationFileDTO.class);

    // When
    DebtPositionTypeOrgOperators result = DebtPositionTypeOrgOperatorMapper.mapToOperators(ingestionFlowFile,debtPositionTypeOrgId,organizationId);

    // Then
    TestUtils.checkNotNullFields(result, "operatorDebtPosTypeOrgId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    Assertions.assertEquals(ingestionFlowFile.getCfOperatorHash(), result.getCfOperatorHash());
    Assertions.assertEquals(organizationId, result.getOrganizationId());
    Assertions.assertEquals(ingestionFlowFile.getDebtPositionTypeOrgCode(), result.getDebtPositionTypeOrgCode());
    Assertions.assertEquals(debtPositionTypeOrgId, result.getDebtPositionTypeOrgId());
    Assertions.assertEquals(ingestionFlowFile.getOrgIpaCode(), result.getOrgIpaCode());
  }
}
