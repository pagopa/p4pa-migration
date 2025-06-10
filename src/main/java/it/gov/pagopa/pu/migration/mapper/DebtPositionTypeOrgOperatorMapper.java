package it.gov.pagopa.pu.migration.mapper;

import it.gov.pagopa.pu.migration.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.model.DebtPositionTypeOrgOperators;

public class DebtPositionTypeOrgOperatorMapper {
  private DebtPositionTypeOrgOperatorMapper() {}

  public static DebtPositionTypeOrgOperators mapToOperators(DebtPositionTypeOrgOperatorMigrationFileDTO dto,
                                                            Long debtPositionTypeOrgId,
                                                            Long organizationId,
                                                            byte[] cfOperatorHash) {
    if (dto == null) {
      return null;
    }
    return DebtPositionTypeOrgOperators.builder()
      .cfOperatorHash(cfOperatorHash)
      .organizationId(organizationId)
      .debtPositionTypeOrgCode(dto.getDebtPositionTypeOrgCode())
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .build();
  }

}
