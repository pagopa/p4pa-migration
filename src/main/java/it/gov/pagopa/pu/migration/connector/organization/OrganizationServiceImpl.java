package it.gov.pagopa.pu.migration.connector.organization;

import it.gov.pagopa.pu.migration.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Lazy
@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationSearchClient organizationSearchClient;


    public OrganizationServiceImpl(OrganizationSearchClient organizationSearchClient) {
        this.organizationSearchClient = organizationSearchClient;
    }



    @Override
    public Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken) {
        return Optional.ofNullable(
                organizationSearchClient.getByIpaCode(ipaCode, accessToken)
        );
    }

}
