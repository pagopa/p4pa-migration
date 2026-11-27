package it.gov.pagopa.pu.migration.service.debtpositiontypeorgoperators;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.migration.dto.generated.ConsumeDebtPositionTypeOrgOperatorsDTO;
import it.gov.pagopa.pu.migration.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.migration.service.AuthorizationService;
import it.gov.pagopa.pu.migration.utils.Utilities;
import it.gov.pagopa.pu.migration.wf.mapper.DebtPositionTypeOrgOperatorMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
public class DebtPositionTypeOrgOperatorsFacadeServiceImpl implements DebtPositionTypeOrgOperatorsFacadeService {
  private final DebtPositionTypeOrgOperatorMapper debtPositionTypeOrgOperatorMapper;
  private final DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository;

  public DebtPositionTypeOrgOperatorsFacadeServiceImpl(DebtPositionTypeOrgOperatorMapper debtPositionTypeOrgOperatorMapper, DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository) {
    this.debtPositionTypeOrgOperatorMapper = debtPositionTypeOrgOperatorMapper;
    this.debtPositionTypeOrgOperatorsRepository = debtPositionTypeOrgOperatorsRepository;
  }

  @Override
  public List<Long> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode, UserInfo loggedUser) {
    AuthorizationService.validateAdminRole(organizationId, loggedUser);
    return debtPositionTypeOrgOperatorsRepository.findUnconsumedByOrganizationIdAndFiscalCodeHash(organizationId, debtPositionTypeOrgOperatorMapper.hashFiscalCode(fiscalCode));
  }

  @Transactional
  @Override
  public void consumeDebtPositionTypeOrgOperators(Long organizationId, ConsumeDebtPositionTypeOrgOperatorsDTO consumeDebtPositionTypeOrgOperatorsDTO, UserInfo loggedUser) {
    AuthorizationService.validateAdminRole(organizationId, loggedUser);
    if(CollectionUtils.isEmpty(consumeDebtPositionTypeOrgOperatorsDTO.getDebtPositionTypeOrgIds())){
      return;
    }

    debtPositionTypeOrgOperatorsRepository.consumeDebtPositionTypeOrgOperators(
      organizationId,
      debtPositionTypeOrgOperatorMapper.hashFiscalCode(consumeDebtPositionTypeOrgOperatorsDTO.getFiscalCode()),
      consumeDebtPositionTypeOrgOperatorsDTO.getDebtPositionTypeOrgIds(),
      OffsetDateTime.now(Utilities.ZONEID)
    );
  }
}
