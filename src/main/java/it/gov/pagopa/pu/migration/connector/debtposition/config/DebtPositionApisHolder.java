package it.gov.pagopa.pu.migration.connector.debtposition.config;

import it.gov.pagopa.pu.debtposition.client.generated.DebtPositionTypeOrgApi;
import it.gov.pagopa.pu.debtposition.client.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtposition.client.generated.DebtPositionTypeOrgSearchControllerApi;
import it.gov.pagopa.pu.debtposition.generated.ApiClient;
import it.gov.pagopa.pu.debtposition.generated.BaseApi;
import it.gov.pagopa.pu.migration.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Lazy
@Service
public class DebtPositionApisHolder {
    private final DebtPositionTypeOrgApi debtPositionTypeOrgApi;
    private final DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityApi;
    private final DebtPositionTypeOrgSearchControllerApi debtPositionTypeOrgSearchControllerApi;

    /** it will store the actual accessToken */
    private final ThreadLocal<String> authContextHolder = new ThreadLocal<>();

    public DebtPositionApisHolder(
            DebtPositionApiClientConfig clientConfig,
            RestTemplateBuilder restTemplateBuilder
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(clientConfig.getBaseUrl());
        apiClient.setBearerToken(authContextHolder::get);
        apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
        apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
        if (clientConfig.isPrintBodyWhenError()) {
            restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("DEBT-POSITIONS"));
        }

        this.debtPositionTypeOrgApi = new DebtPositionTypeOrgApi(apiClient);
        this.debtPositionTypeOrgEntityApi = new DebtPositionTypeOrgEntityControllerApi(apiClient);
        this.debtPositionTypeOrgSearchControllerApi = new DebtPositionTypeOrgSearchControllerApi(apiClient);
    }

    @PreDestroy
    public void unload() {
        authContextHolder.remove();
    }


    public DebtPositionTypeOrgApi getDebtPositionTypeOrgApi(String accessToken) {
        return getApi(accessToken, debtPositionTypeOrgApi);
    }

    public DebtPositionTypeOrgEntityControllerApi getDebtPositionTypeOrgEntityApi(String accessToken) {
        return getApi(accessToken,  debtPositionTypeOrgEntityApi);
    }

    public DebtPositionTypeOrgSearchControllerApi getDebtPositionTypeOrgSearchControllerApi(String accessToken) {
        return getApi(accessToken,  debtPositionTypeOrgSearchControllerApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, T api) {
        authContextHolder.set(accessToken);
        return api;
    }
}
