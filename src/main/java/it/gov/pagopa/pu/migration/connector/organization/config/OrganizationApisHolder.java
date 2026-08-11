package it.gov.pagopa.pu.migration.connector.organization.config;

import it.gov.pagopa.pu.migration.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.organization.client.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationErrorDTO;
import it.gov.pagopa.pu.organization.generated.ApiClient;
import it.gov.pagopa.pu.organization.generated.BaseApi;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Lazy
@Service
public class OrganizationApisHolder {
    private final OrganizationSearchControllerApi organizationSearchControllerApi;

    private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();
    private final OrganizationEntityControllerApi organizationEntityControllerApi;

    public OrganizationApisHolder(
        OrganizationApiClientConfig clientConfig,
        RestTemplateBuilder restTemplateBuilder,
        JsonMapper jsonMapper
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(clientConfig.getBaseUrl());
        apiClient.setBearerToken(bearerTokenHolder::get);
        apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
        apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
        restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "ORGANIZATION", clientConfig.isPrintBodyWhenError(),
          OrganizationErrorDTO.class, OrganizationErrorDTO::getCode, OrganizationErrorDTO::getMessage)
        );

        this.organizationSearchControllerApi = new OrganizationSearchControllerApi(apiClient);
        this.organizationEntityControllerApi = new OrganizationEntityControllerApi(apiClient);
    }

    @PreDestroy
    public void unload(){
        bearerTokenHolder.remove();
    }

    /** It will return a {@link OrganizationSearchControllerApi} instrumented with the provided accessToken. Use null if auth is not required */
    public OrganizationSearchControllerApi getOrganizationSearchControllerApi(String accessToken){
        return getApi(accessToken, organizationSearchControllerApi);
    }

  public OrganizationEntityControllerApi getOrganizationEntityControllerApi(String accessToken) {
    return getApi(accessToken, organizationEntityControllerApi);
  }


  private <T extends BaseApi> T getApi(String accessToken, T api) {
        bearerTokenHolder.set(accessToken);
        return api;
    }
}
