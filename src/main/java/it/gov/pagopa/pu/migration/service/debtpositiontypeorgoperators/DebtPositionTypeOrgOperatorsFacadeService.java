package it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;

import java.util.List;

public interface DebtPositionTypeOrgOperatorsFacadeService {
  List<Long> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode, UserInfo loggedUser);
}
