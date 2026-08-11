package it.gov.pagopa.pu.migration.connector.organization.client;

import it.gov.pagopa.pu.migration.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.migration.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@Slf4j
public class OrganizationSearchClient {

    private final OrganizationApisHolder organizationApisHolder;

    public OrganizationSearchClient(OrganizationApisHolder organizationApisHolder) {
        this.organizationApisHolder = organizationApisHolder;
    }

    public Organization getByIpaCode(String ipaCode, String accessToken) {
        try {
            return organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
                    .crudOrganizationsFindByIpaCode(ipaCode);
        } catch (RestInvokeNotFoundException e) {
            log.info("Organization not found: ipaCode: {}", ipaCode);
            return null;
        }
    }

  public Organization getByOrganizationId(Long organizationId, String accessToken) {
    try{
      return organizationApisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization(String.valueOf(organizationId));
    } catch (RestInvokeNotFoundException e){
      log.info("Cannot find organization having organizationId {}", organizationId);
      return null;
    }
  }

}
