package it.gov.pagopa.pu.migration.wf.mapper;

import it.gov.pagopa.pu.migration.wf.dto.debtpositiontypeorgoperator.DebtPositionTypeOrgOperatorMigrationFileDTO;
import it.gov.pagopa.pu.migration.model.OperatorsDebtPosTypeOrg;
import it.gov.pagopa.pu.migration.utils.AESUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionTypeOrgOperatorMapper {
  private final String dataCipherPsw;

  public DebtPositionTypeOrgOperatorMapper(
    @Value("${encryption.file-encrypt-password}") String dataCipherPsw) {
    this.dataCipherPsw = dataCipherPsw;
  }

  public OperatorsDebtPosTypeOrg mapToOperators(DebtPositionTypeOrgOperatorMigrationFileDTO dto,
                                                Long debtPositionTypeOrgId,
                                                Long organizationId) {
    if (dto == null) {
      return null;
    }
    byte [] cfOperatorHash = AESUtils.encrypt(this.dataCipherPsw, dto.getCfOperator());
    return OperatorsDebtPosTypeOrg.builder()
      .cfOperatorHash(cfOperatorHash)
      .organizationId(organizationId)
      .debtPositionTypeOrgCode(dto.getDebtPositionTypeOrgCode())
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .build();
  }

}
