package it.gov.pagopa.pu.migration.connector.organization.client;

import it.gov.pagopa.pu.migration.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.client.generated.OrganizationEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class OrganizationSearchClientTest {

    @Mock
    private OrganizationApisHolder organizationApisHolderMock;
    @Mock
    private OrganizationSearchControllerApi organizationSearchControllerApiMock;
    @Mock
    private OrganizationEntityControllerApi organizationEntityControllerApiMock;

    private OrganizationSearchClient organizationSearchClient;

    @BeforeEach
    void setUp() {
        organizationSearchClient = new OrganizationSearchClient(organizationApisHolderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                organizationApisHolderMock
        );
    }

    @Test
    void whenGetByIpaCodeThenInvokeWithAccessToken(){
        // Given
        String accessToken = "ACCESSTOKEN";
        String orgIpaCode = "ORGIPACODE";
        Organization expectedResult = new Organization();

        Mockito.when(organizationApisHolderMock.getOrganizationSearchControllerApi(accessToken))
                .thenReturn(organizationSearchControllerApiMock);
        Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByIpaCode(orgIpaCode))
                .thenReturn(expectedResult);

        // When
        Organization result = organizationSearchClient.getByIpaCode(orgIpaCode, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
    }

  @Test
  void givenNotExistentOrganizationWhenGetByIpaCodeThenNull(){
    // Given
    String accessToken = "ACCESSTOKEN";
    String orgIpaCode = "ORGIPACODE";

    Mockito.when(organizationApisHolderMock.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByIpaCode(orgIpaCode))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Organization result = organizationSearchClient.getByIpaCode(orgIpaCode, accessToken);

    // Then
    Assertions.assertNull(result);
  }


  @Test
  void whenGetByIdThenInvokeWithAccessToken(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;
    Organization expectedResult = new Organization();

    Mockito.when(organizationApisHolderMock.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(orgId.toString()))
      .thenReturn(expectedResult);

    // When
    Organization result = organizationSearchClient.getByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNotExistentOrganizationWhenGetByIdThenNull(){
    // Given
    String accessToken = "ACCESSTOKEN";
    Long orgId = 1L;

    Mockito.when(organizationApisHolderMock.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    Mockito.when(organizationEntityControllerApiMock.crudGetOrganization(orgId.toString()))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    Organization result = organizationSearchClient.getByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }





}
