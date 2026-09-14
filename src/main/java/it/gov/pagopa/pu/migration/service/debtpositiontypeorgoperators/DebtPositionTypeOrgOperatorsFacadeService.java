package it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.dto.generated.ConsumeDebtPositionTypeOrgOperatorsDTO;

import java.util.List;

public interface DebtPositionTypeOrgOperatorsFacadeService {
  List<Long> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode, UserInfo loggedUser);
  void consumeDebtPositionTypeOrgOperators(Long organizationId, String fiscalCode, ConsumeDebtPositionTypeOrgOperatorsDTO consumeDebtPositionTypeOrgOperatorsDTO, UserInfo loggedUser);
}
