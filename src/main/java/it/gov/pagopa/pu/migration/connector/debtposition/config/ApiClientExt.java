package it.gov.pagopa.pu.migration.connector.debtposition.config;

import it.gov.pagopa.pu.debtposition.generated.ApiClient;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Setter
public class ApiClientExt extends ApiClient {

    public static final String HEADER_USER_ID = "X-user-id";

    public ApiClientExt(RestTemplate restTemplate){
        super(restTemplate);
    }

    private Supplier<String> userIdSupplier;

    @Override
    public <T> ResponseEntity<T> invokeAPI(String path, HttpMethod method, Map<String, Object> pathParams, MultiValueMap<String, String> queryParams, Object body, HttpHeaders headerParams, MultiValueMap<String, String> cookieParams, MultiValueMap<String, Object> formParams, List<MediaType> accept, MediaType contentType, String[] authNames, ParameterizedTypeReference<T> returnType) throws RestClientException {
        String userId = userIdSupplier.get();
        if(!StringUtils.isEmpty(userId)){
            headerParams.put(HEADER_USER_ID, List.of(userId));
        }
        return super.invokeAPI(path, method, pathParams, queryParams, body, headerParams, cookieParams, formParams, accept, contentType, authNames, returnType);
    }
}
