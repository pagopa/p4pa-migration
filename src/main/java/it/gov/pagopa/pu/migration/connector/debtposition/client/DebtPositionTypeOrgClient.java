package it.gov.pagopa.pu.migration.connector.debtposition.client;

import it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionTypeOrg;
import it.gov.pagopa.pu.migration.connector.debtposition.config.DebtPositionApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Lazy
@Service
@Slf4j
public class DebtPositionTypeOrgClient {

    private final DebtPositionApisHolder debtPositionApisHolder;

    public DebtPositionTypeOrgClient(DebtPositionApisHolder debtPositionApisHolder) {
        this.debtPositionApisHolder = debtPositionApisHolder;
    }

    public DebtPositionTypeOrg getById(Long debtPositionTypeOrgId, String accessToken) {
        try {
            return debtPositionApisHolder.getDebtPositionTypeOrgEntityApi(accessToken)
                    .crudGetDebtpositiontypeorg(String.valueOf(debtPositionTypeOrgId));
        } catch (HttpClientErrorException.NotFound e){
            log.info("Cannot find DebtPositionTypeOrg having id {}", debtPositionTypeOrgId);
            return null;
        }
    }



    public DebtPositionTypeOrg getDebtPositionTypeOrgByCodeAndOrgId(String code, Long orgId, String accessToken) {
        try {
            return debtPositionApisHolder.getDebtPositionTypeOrgSearchControllerApi(accessToken)
                    .crudDebtPositionTypeOrgsFindByOrganizationIdAndCode(orgId, code);
        } catch (HttpClientErrorException.NotFound e){
            log.info("Cannot find DebtPositionTypeOrg having organizationId {} and code {}", orgId, code);
            return null;
        }
    }
}
