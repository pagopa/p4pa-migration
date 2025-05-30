package it.gov.pagopa.pu.migration.connector.debtposition.config;

import it.gov.pagopa.pu.debtposition.client.generated.*;
import it.gov.pagopa.pu.debtposition.generated.ApiClient;
import it.gov.pagopa.pu.debtposition.generated.BaseApi;
import it.gov.pagopa.pu.migration.config.rest.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Lazy
@Service
public class DebtPositionApisHolder {
    private final DebtPositionTypeOrgApi debtPositionTypeOrgApi;
    private final DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityApi;
    private final DebtPositionTypeOrgSearchControllerApi debtPositionTypeOrgSearchControllerApi;

    /** it will store the actual accessToken and mappedExternalUserId */
    private final ThreadLocal<Pair<String, String>> authContextHolder = new ThreadLocal<>();

    public DebtPositionApisHolder(
            DebtPositionApiClientConfig clientConfig,
            RestTemplateBuilder restTemplateBuilder
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(clientConfig.getBaseUrl());
        apiClient.setBearerToken(() -> authContextHolder.get().getKey());
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
        return getApi(accessToken, null, debtPositionTypeOrgApi);
    }

    public DebtPositionTypeOrgEntityControllerApi getDebtPositionTypeOrgEntityApi(String accessToken) {
        return getApi(accessToken, null, debtPositionTypeOrgEntityApi);
    }

    public DebtPositionTypeOrgSearchControllerApi getDebtPositionTypeOrgSearchControllerApi(String accessToken) {
        return getApi(accessToken, null, debtPositionTypeOrgSearchControllerApi);
    }

    private <T extends BaseApi> T getApi(String accessToken, String mappedExternalUserId, T api) {
        authContextHolder.set(Pair.of(accessToken, mappedExternalUserId));
        return api;
    }
}
