package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.migration.utils.TestUtils;
import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.wf.mapper.DebtPositionTypeOrgOperatorMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

class DebtPositionTypeOrgOperatorMapperTest {

  private DebtPositionTypeOrgOperatorMapper mapper;

  @BeforeEach
    void setUp() {
        mapper = new DebtPositionTypeOrgOperatorMapper("PEPPER");
    }


  @Test
  void test() {
    // Given
    Long debtPositionTypeOrgId = 0L;
    Long organizationId = 1L;
    DebtPositionTypeOrgOperatorMigrationFileDTO migrationFileDTO = TestUtils.getPodamFactory().manufacturePojo(DebtPositionTypeOrgOperatorMigrationFileDTO.class);

    // When
    DebtPositionTypeOrgOperators result = mapper.mapToOperators(migrationFileDTO,debtPositionTypeOrgId,organizationId);

    // Then
    TestUtils.checkNotNullFields(result, "debtPositionTypeOrgOperatorId", "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId", "consumptionDateTime");
    Assertions.assertEquals(organizationId, result.getOrganizationId());
    Assertions.assertEquals(migrationFileDTO.getDebtPositionTypeOrgCode(), result.getDebtPositionTypeOrgCode());
    Assertions.assertEquals(debtPositionTypeOrgId, result.getDebtPositionTypeOrgId());
  }

  @Test
  void whenHashFiscalCodeThenOk() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] hash = mapper.hashFiscalCode(plain);

    // Then
    Assertions.assertEquals("s+QUCtO7vYNzHCDrH03EVRGPZTyfIXwBKTRrgYWqwc4=", Base64.getEncoder().encodeToString(hash));
  }

  @Test
  void givenNullWhenHashFiscalCodeThenNull() {
    // When
    byte[] hash = mapper.hashFiscalCode(null);

    // Then
    Assertions.assertNull(hash);
  }
}
