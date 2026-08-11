package it.gov.pagopa.pu.migration.connector.debtposition.config;

import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.client.generated.DebtPositionTypeOrgSearchControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionErrorDTO;
import it.gov.pagopa.pu.debtpositions.generated.ApiClient;
import it.gov.pagopa.pu.debtpositions.generated.BaseApi;
import it.gov.pagopa.pu.migration.config.rest.HttpClientErrorJsonBodyHandler;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Lazy
@Service
public class DebtPositionApisHolder {
    private final DebtPositionTypeOrgEntityControllerApi debtPositionTypeOrgEntityApi;
    private final DebtPositionTypeOrgSearchControllerApi debtPositionTypeOrgSearchControllerApi;

    /** it will store the actual accessToken */
    private final ThreadLocal<String> authContextHolder = new ThreadLocal<>();

    public DebtPositionApisHolder(
            DebtPositionApiClientConfig clientConfig,
            RestTemplateBuilder restTemplateBuilder,
            JsonMapper jsonMapper
    ) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(clientConfig.getBaseUrl());
        apiClient.setBearerToken(authContextHolder::get);
        apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
        apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
        restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "DEBT-POSITIONS", clientConfig.isPrintBodyWhenError(),
          DebtPositionErrorDTO.class, DebtPositionErrorDTO::getCode, DebtPositionErrorDTO::getMessage)
        );

        this.debtPositionTypeOrgEntityApi = new DebtPositionTypeOrgEntityControllerApi(apiClient);
        this.debtPositionTypeOrgSearchControllerApi = new DebtPositionTypeOrgSearchControllerApi(apiClient);
    }

    @PreDestroy
    public void unload() {
        authContextHolder.remove();
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
