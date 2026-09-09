package it.gov.pagopa.pu.migration.controller;

import it.gov.pagopa.pu.migration.controller.generated.DebtPositionTypeOrgOperatorsApi;
import it.gov.pagopa.pu.migration.security.SecurityUtils;
import it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators.DebtPositionTypeOrgOperatorsFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class DebtPositionTypeOrgOperatorsControllerImpl implements DebtPositionTypeOrgOperatorsApi {
  private final DebtPositionTypeOrgOperatorsFacadeService debtPositionTypeOrgOperatorsFacadeService;

  public DebtPositionTypeOrgOperatorsControllerImpl(DebtPositionTypeOrgOperatorsFacadeService debtPositionTypeOrgOperatorsFacadeService) {
    this.debtPositionTypeOrgOperatorsFacadeService = debtPositionTypeOrgOperatorsFacadeService;
  }

  @Override
  public ResponseEntity<List<Long>> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode) {
    log.info("Retrieving unconsumed DebtPositionTypeOrg ids having organizationId {}", organizationId);
    return ResponseEntity.ok(debtPositionTypeOrgOperatorsFacadeService.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode, SecurityUtils.getLoggedUser()));
  }
}
