package it.gov.pagopa.pu.migration.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

import java.util.Optional;

public interface OrganizationService {

  Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken);

}
