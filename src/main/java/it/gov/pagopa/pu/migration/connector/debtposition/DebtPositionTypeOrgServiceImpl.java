package it.gov.pagopa.pu.migration.connector.debtposition;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtposition.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.migration.connector.debtposition.client.DebtPositionTypeOrgClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Lazy
@Service
@Slf4j
public class DebtPositionTypeOrgServiceImpl implements DebtPositionTypeOrgService {

    private final DebtPositionTypeOrgClient debtPositionTypeOrgClient;

    public DebtPositionTypeOrgServiceImpl(DebtPositionTypeOrgClient debtPositionTypeOrgClient) {
        this.debtPositionTypeOrgClient = debtPositionTypeOrgClient;
    }


    @Override
    public Optional<DebtPositionTypeOrg> getDebtPositionTypeOrgByCodeAndOrgId(String code, Long orgId, String accessToken) {
        log.info("Find DebtPositionTypeOrg by code: {} and organization id: {}", code, orgId);
        return Optional.ofNullable(debtPositionTypeOrgClient.getDebtPositionTypeOrgByCodeAndOrgId(code, orgId, accessToken));
    }
}
