package it.gov.pagopa.pu.migration.connector.organization.client;

import it.gov.pagopa.pu.migration.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.migration.exception.common.RestInvokeNotFoundException;
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

import static org.mockito.Mockito.when;

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

        when(organizationApisHolderMock.getOrganizationSearchControllerApi(accessToken))
                .thenReturn(organizationSearchControllerApiMock);
        when(organizationSearchControllerApiMock.crudOrganizationsFindByIpaCode(orgIpaCode))
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

    when(organizationApisHolderMock.getOrganizationSearchControllerApi(accessToken))
      .thenReturn(organizationSearchControllerApiMock);
    when(organizationSearchControllerApiMock.crudOrganizationsFindByIpaCode(orgIpaCode))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

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

    when(organizationApisHolderMock.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    when(organizationEntityControllerApiMock.crudGetOrganization(orgId.toString()))
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

    when(organizationApisHolderMock.getOrganizationEntityControllerApi(accessToken))
      .thenReturn(organizationEntityControllerApiMock);
    when(organizationEntityControllerApiMock.crudGetOrganization(orgId.toString()))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    // When
    Organization result = organizationSearchClient.getByOrganizationId(orgId, accessToken);

    // Then
    Assertions.assertNull(result);
  }





}
